package com.charles.jurysim.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.jurysim.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JurorProfileUiState(
    val name: String = "",
    val age: String = "30",
    val occupation: String = "",
    val education: String = "",
    val hasLegalExperience: Boolean = false,
    val hasBeenVictim: Boolean = false,
    val politicalLeaning: String = "Moderate",
    val additionalInfo: String = ""
)

class JurorProfileViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JurorProfileUiState())
    val uiState: StateFlow<JurorProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = preferencesRepository.getJurorProfile()
            _uiState.value = JurorProfileUiState(
                name = profile.name,
                age = profile.age.toString(),
                occupation = profile.occupation,
                education = profile.education,
                hasLegalExperience = profile.hasLegalExperience,
                hasBeenVictim = profile.hasBeenVictim,
                politicalLeaning = profile.politicalLeaning,
                additionalInfo = profile.additionalInfo
            )
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateAge(age: String) {
        _uiState.value = _uiState.value.copy(age = age)
    }

    fun updateOccupation(occupation: String) {
        _uiState.value = _uiState.value.copy(occupation = occupation)
    }

    fun updateEducation(education: String) {
        _uiState.value = _uiState.value.copy(education = education)
    }

    fun updateHasLegalExperience(hasExperience: Boolean) {
        _uiState.value = _uiState.value.copy(hasLegalExperience = hasExperience)
    }

    fun updateHasBeenVictim(hasBeenVictim: Boolean) {
        _uiState.value = _uiState.value.copy(hasBeenVictim = hasBeenVictim)
    }

    fun updatePoliticalLeaning(leaning: String) {
        _uiState.value = _uiState.value.copy(politicalLeaning = leaning)
    }

    fun updateAdditionalInfo(info: String) {
        _uiState.value = _uiState.value.copy(additionalInfo = info)
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            preferencesRepository.saveJurorProfile(
                name = state.name,
                age = state.age.toIntOrNull() ?: 30,
                occupation = state.occupation,
                education = state.education,
                hasLegalExperience = state.hasLegalExperience,
                hasBeenVictim = state.hasBeenVictim,
                politicalLeaning = state.politicalLeaning,
                additionalInfo = state.additionalInfo
            )
        }
    }
}
