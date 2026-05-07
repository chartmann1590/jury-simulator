package com.jurysim.ui.screens.onboarding

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jurysim.data.llm.DownloadState
import com.jurysim.data.llm.LlmEngineProvider
import com.jurysim.data.llm.ModelDownloader
import com.jurysim.data.llm.ModelManager
import com.jurysim.data.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class OnboardingUiState(
    val download: DownloadState = DownloadState.Idle,
    val totalRamBytes: Long = 0L,
    val freeSpaceBytes: Long = 0L,
    val customUrl: String? = null
) {
    /** Show a "this device may struggle" callout when RAM is tight. */
    val lowRamWarning: Boolean
        get() = totalRamBytes in 1L..3_900_000_000L

    val notEnoughSpace: Boolean
        get() = freeSpaceBytes in 1L..4_499_999_999L
}

/**
 * Drives the onboarding screen. Holds the live [DownloadState] from
 * [ModelDownloader], persists `model_ready=true` once the engine loads, and
 * exposes a couple of action methods (`startDownload`, `cancelDownload`,
 * `retry`, `useCustomUrl`).
 */
class OnboardingViewModel(
    application: Application,
    private val modelManager: ModelManager,
    private val modelDownloader: ModelDownloader,
    private val llmEngineProvider: LlmEngineProvider,
    private val preferencesRepository: PreferencesRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null

    init {
        refreshDeviceInfo()

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

    fun useCustomUrl(url: String) {
        _uiState.value = _uiState.value.copy(customUrl = url.trim().takeIf { it.isNotEmpty() })
    }

    fun startDownload() {
        if (downloadJob?.isActive == true) return
        refreshDeviceInfo()

        val url = _uiState.value.customUrl
        downloadJob = viewModelScope.launch {
            val flow = if (url.isNullOrBlank()) modelDownloader.download()
                       else modelDownloader.download(url)
            flow.collect { state ->
                _uiState.value = _uiState.value.copy(download = state)
                if (state is DownloadState.Ready) {
                    loadEngineAndFinish()
                }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _uiState.value = _uiState.value.copy(download = DownloadState.Idle)
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(download = DownloadState.Idle)
        startDownload()
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
}
