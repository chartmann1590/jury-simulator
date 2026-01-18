package com.jurysim.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurysim.data.model.OllamaModel
import com.jurysim.data.repository.OllamaRepository
import com.jurysim.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val serverUrl: String = "",
    val selectedModel: String = "",
    val models: List<OllamaModel> = emptyList(),
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val error: String? = null
)

class SettingsViewModel(
    private val ollamaRepository: OllamaRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val serverUrl = preferencesRepository.serverUrl.first() ?: ""
            val selectedModel = preferencesRepository.selectedModel.first() ?: ""

            _uiState.value = _uiState.value.copy(
                serverUrl = serverUrl,
                selectedModel = selectedModel
            )

            if (serverUrl.isNotBlank()) {
                testConnection()
            }
        }
    }

    fun updateServerUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            serverUrl = url,
            error = null,
            isConnected = false
        )
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            preferencesRepository.saveServerUrl(_uiState.value.serverUrl)
            ollamaRepository.updateBaseUrl(_uiState.value.serverUrl)

            val result = ollamaRepository.getModels()

            result.onSuccess { models ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = true,
                    models = models,
                    error = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = false,
                    error = error.message ?: "Failed to connect to server"
                )
            }
        }
    }

    fun selectModel(modelName: String) {
        viewModelScope.launch {
            preferencesRepository.saveSelectedModel(modelName)
            _uiState.value = _uiState.value.copy(selectedModel = modelName)
        }
    }
}
