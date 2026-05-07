package com.jurysim.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurysim.data.llm.LlmEngineProvider
import com.jurysim.data.llm.ModelManager
import com.jurysim.data.repository.PreferencesRepository
import com.jurysim.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SettingsUiState(
    val modelName: String = Constants.LITERTLM_MODEL_DISPLAY_NAME,
    val modelFilename: String = Constants.LITERTLM_MODEL_FILENAME,
    val modelSizeBytes: Long = 0L,
    val isModelPresent: Boolean = false,
    val ttsEnabled: Boolean = true,
    val pendingAction: PendingAction? = null
)

enum class PendingAction { CONFIRM_DELETE, CONFIRM_REDOWNLOAD }

class SettingsViewModel(
    private val modelManager: ModelManager,
    private val llmEngineProvider: LlmEngineProvider,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
        observeTtsSetting()
    }

    private fun observeTtsSetting() {
        viewModelScope.launch {
            preferencesRepository.ttsEnabled.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(ttsEnabled = enabled)
            }
        }
    }

    private fun refresh() {
        _uiState.value = _uiState.value.copy(
            modelSizeBytes = modelManager.modelSizeOnDiskBytes(),
            isModelPresent = modelManager.isModelDownloaded()
        )
    }

    fun requestDelete() {
        _uiState.value = _uiState.value.copy(pendingAction = PendingAction.CONFIRM_DELETE)
    }

    fun requestRedownload() {
        _uiState.value = _uiState.value.copy(pendingAction = PendingAction.CONFIRM_REDOWNLOAD)
    }

    fun cancelPendingAction() {
        _uiState.value = _uiState.value.copy(pendingAction = null)
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setTtsEnabled(enabled)
        }
    }

    /**
     * Confirms either pending action: drops the engine, deletes the model
     * file, and clears the model_ready preference. Returns control to the
     * caller (the Settings screen) which navigates back to Onboarding.
     */
    fun confirmPendingAction(onCleared: () -> Unit) {
        val pending = _uiState.value.pendingAction ?: return
        viewModelScope.launch {
            llmEngineProvider.reset()
            modelManager.deleteAll()
            preferencesRepository.setModelReady(false)
            _uiState.value = _uiState.value.copy(
                pendingAction = null,
                isModelPresent = false,
                modelSizeBytes = 0L
            )
            // Both actions land in the same place — the onboarding flow.
            // The screen distinguishes them only in the confirmation copy.
            @Suppress("UNUSED_VARIABLE")
            val unused = pending
            onCleared()
        }
    }
}
