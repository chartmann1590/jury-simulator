package com.charles.jurysim.ui.screens.onboarding

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.charles.jurysim.data.llm.DownloadState
import com.charles.jurysim.data.llm.LlmEngineProvider
import com.charles.jurysim.data.llm.ModelDownloadWorker
import com.charles.jurysim.data.llm.ModelManager
import com.charles.jurysim.data.repository.PreferencesRepository
import com.charles.jurysim.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class OnboardingUiState(
    val download: DownloadState = DownloadState.Idle,
    val totalRamBytes: Long = 0L,
    val freeSpaceBytes: Long = 0L,
    val customUrl: String? = null,
    val customUrlError: String? = null
) {
    /** Show a "this device may struggle" callout when RAM is tight. */
    val lowRamWarning: Boolean
        get() = totalRamBytes in 1L..3_900_000_000L

    val notEnoughSpace: Boolean
        get() = freeSpaceBytes in 1L..4_499_999_999L
}

/**
 * Drives the onboarding screen.
 *
 * The 3.65 GB model download runs in [ModelDownloadWorker] (a
 * WorkManager foreground service, type dataSync) so it survives the
 * user leaving the app or the process being reclaimed. This ViewModel
 * enqueues and observes that work, mapping WorkInfo back onto the
 * [DownloadState] the UI already renders, and persists
 * `model_ready=true` once the engine loads successfully.
 */
class OnboardingViewModel(
    application: Application,
    private val modelManager: ModelManager,
    private val llmEngineProvider: LlmEngineProvider,
    private val preferencesRepository: PreferencesRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val workManager = WorkManager.getInstance(application)

    init {
        refreshDeviceInfo()
        observeDownloadWork()

        // If the file is already on disk (e.g. reinstalled prefs but kept
        // filesDir), skip straight to engine load.
        if (modelManager.isModelDownloaded()) {
            loadEngineAndFinish()
        }
    }

    private fun refreshDeviceInfo() {
        val ctx = getApplication<Application>().applicationContext
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo().also { am?.getMemoryInfo(it) }
        _uiState.value = _uiState.value.copy(
            totalRamBytes = memInfo.totalMem,
            freeSpaceBytes = ctx.filesDir.usableSpace
        )
    }

    /**
     * Validates and stores a user-supplied model URL.
     *
     * Rules (strict — a bad URL here means a 3+ GB download of the wrong
     * thing, or worse): HTTPS only, a real host, and a path ending in
     * `.litertlm`. An empty value resets to the pinned default model.
     * Validation failures surface in [OnboardingUiState.customUrlError]
     * and leave the previous value untouched.
     */
    /** Returns true if the value was accepted (or cleared), false if it
     *  failed validation — the dialog stays open and shows the error. */
    fun useCustomUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = _uiState.value.copy(customUrl = null, customUrlError = null)
            return true
        }
        val error = validateCustomModelUrl(trimmed)
        _uiState.value = if (error == null) {
            _uiState.value.copy(customUrl = trimmed, customUrlError = null)
        } else {
            _uiState.value.copy(customUrlError = error)
        }
        return error == null
    }

    fun startDownload() {
        refreshDeviceInfo()
        if (!modelManager.hasFreeSpace()) {
            _uiState.value = _uiState.value.copy(
                download = DownloadState.Failed(
                    "Not enough free storage. Free up at least 4.5 GB and try again.",
                    retryable = false
                )
            )
            return
        }
        val url = _uiState.value.customUrl ?: Constants.LITERTLM_MODEL_URL
        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ModelDownloadWorker.KEY_URL, url)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork(
            ModelDownloadWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelDownload() {
        workManager.cancelUniqueWork(ModelDownloadWorker.UNIQUE_WORK_NAME)
        _uiState.value = _uiState.value.copy(download = DownloadState.Idle)
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(download = DownloadState.Idle)
        startDownload()
    }

    /** Maps the unique download work's state onto [DownloadState]. */
    private fun observeDownloadWork() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(ModelDownloadWorker.UNIQUE_WORK_NAME)
                .collect { infos ->
                    val info = infos.lastOrNull() ?: return@collect
                    // Don't clobber the engine-loading / ready states that
                    // loadEngineAndFinish() drives after a successful run.
                    val current = _uiState.value.download
                    if (current is DownloadState.LoadingEngine || current is DownloadState.Ready) {
                        return@collect
                    }
                    val mapped = mapWorkInfo(info)
                    if (mapped != null) {
                        _uiState.value = _uiState.value.copy(download = mapped)
                        if (mapped is DownloadState.Ready) {
                            loadEngineAndFinish()
                        }
                    }
                }
        }
    }

    private fun mapWorkInfo(info: WorkInfo): DownloadState? {
        return when (info.state) {
            WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> DownloadState.Connecting
            WorkInfo.State.RUNNING -> {
                val downloaded = info.progress.getLong(ModelDownloadWorker.KEY_BYTES_DOWNLOADED, 0L)
                val bytesPerSec = info.progress.getLong(ModelDownloadWorker.KEY_BYTES_PER_SEC, 0L)
                val total = info.progress.getLong(
                    ModelDownloadWorker.KEY_TOTAL_BYTES,
                    Constants.LITERTLM_MODEL_SIZE_BYTES
                )
                if (downloaded <= 0L) {
                    DownloadState.Connecting
                } else if (downloaded >= total) {
                    DownloadState.Verifying
                } else {
                    DownloadState.Downloading(
                        bytesDownloaded = downloaded,
                        totalBytes = total,
                        bytesPerSec = bytesPerSec
                    )
                }
            }
            WorkInfo.State.SUCCEEDED -> DownloadState.Ready
            WorkInfo.State.FAILED -> DownloadState.Failed(
                info.outputData.getString(ModelDownloadWorker.KEY_ERROR)
                    ?: "Download failed"
            )
            WorkInfo.State.CANCELLED -> DownloadState.Idle
        }
    }

    private fun loadEngineAndFinish() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(download = DownloadState.LoadingEngine)
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    llmEngineProvider.get().ensureLoaded()
                }
            }
            if (result.isSuccess) {
                preferencesRepository.setModelReady(true)
                _uiState.value = _uiState.value.copy(download = DownloadState.Ready)
            } else {
                // Engine init failed — model file is on disk but unusable.
                modelManager.deleteAll()
                preferencesRepository.setModelReady(false)
                _uiState.value = _uiState.value.copy(
                    download = DownloadState.Failed(
                        result.exceptionOrNull()?.message
                            ?: "Failed to load model into LiteRT-LM"
                    )
                )
            }
        }
    }

    companion object {
        /** Returns a user-facing error, or null if [url] is acceptable. */
        fun validateCustomModelUrl(url: String): String? {
            val uri = runCatching { java.net.URI(url) }.getOrNull()
                ?: return "That doesn't look like a valid URL."
            if (!uri.scheme.equals("https", ignoreCase = true)) {
                return "Custom model URLs must use HTTPS."
            }
            if (uri.host.isNullOrBlank()) {
                return "The URL has no host."
            }
            val path = uri.path ?: return "The URL must point to a .litertlm file."
            if (!path.endsWith(".litertlm", ignoreCase = true)) {
                return "The URL must point to a .litertlm file."
            }
            return null
        }
    }
}
