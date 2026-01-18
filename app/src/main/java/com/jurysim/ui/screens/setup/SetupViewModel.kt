package com.jurysim.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurysim.data.repository.OllamaRepository
import com.jurysim.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SetupUiState(
    val serverUrl: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val error: String? = null
)

class SetupViewModel(
    private val ollamaRepository: OllamaRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.serverUrl.collect { savedUrl ->
                savedUrl?.let {
                    _uiState.value = _uiState.value.copy(serverUrl = it)
                }
            }
        }
    }

    fun updateServerUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            serverUrl = url,
            isConnected = false,
            error = null
        )
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            ollamaRepository.updateBaseUrl(_uiState.value.serverUrl)
            val result = ollamaRepository.testConnection()

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = true,
                    error = null
                )
                preferencesRepository.saveServerUrl(_uiState.value.serverUrl)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = false,
                    error = error.message ?: "Connection failed"
                )
            }
        }
    }
}
