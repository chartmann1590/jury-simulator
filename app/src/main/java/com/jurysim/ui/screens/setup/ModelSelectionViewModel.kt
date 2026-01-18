package com.jurysim.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurysim.data.model.OllamaModel
import com.jurysim.data.repository.OllamaRepository
import com.jurysim.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModelSelectionUiState(
    val models: List<OllamaModel> = emptyList(),
    val selectedModel: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ModelSelectionViewModel(
    private val ollamaRepository: OllamaRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelSelectionUiState())
    val uiState: StateFlow<ModelSelectionUiState> = _uiState.asStateFlow()

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = ollamaRepository.getModels()

            result.onSuccess { models ->
                _uiState.value = _uiState.value.copy(
                    models = models,
                    isLoading = false,
                    error = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    models = emptyList(),
                    isLoading = false,
                    error = error.message ?: "Failed to load models"
                )
            }
        }
    }

    fun selectModel(modelName: String) {
        _uiState.value = _uiState.value.copy(selectedModel = modelName)
        viewModelScope.launch {
            preferencesRepository.saveSelectedModel(modelName)
        }
    }

    fun retry() {
        loadModels()
    }
}
