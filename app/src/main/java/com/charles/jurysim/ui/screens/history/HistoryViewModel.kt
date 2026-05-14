package com.charles.jurysim.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.jurysim.data.local.CaseEntity
import com.charles.jurysim.data.repository.CaseHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val cases: List<CaseEntity> = emptyList(),
    val selectedCase: CaseEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HistoryViewModel(
    private val caseHistoryRepository: CaseHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadCases()
    }

    private fun loadCases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            caseHistoryRepository.getAllCases().collect { cases ->
                _uiState.value = _uiState.value.copy(
                    cases = cases,
                    isLoading = false
                )
            }
        }
    }

    fun selectCase(case: CaseEntity) {
        _uiState.value = _uiState.value.copy(selectedCase = case)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedCase = null)
    }

    fun deleteCase(caseId: Long) {
        viewModelScope.launch {
            caseHistoryRepository.deleteCase(caseId)
            clearSelection()
        }
    }

    fun deleteAllCases() {
        viewModelScope.launch {
            caseHistoryRepository.deleteAllCases()
            clearSelection()
        }
    }
}
