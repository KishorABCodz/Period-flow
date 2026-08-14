package com.periodflow.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodflow.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 1,
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val hasPolycysticOvaries: Boolean? = null,
    val acneSeverity: String? = null,
    val hirsutismSeverity: String? = null,
)

sealed interface OnboardingEvent {
    data object OnboardingFinished : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<OnboardingEvent>()
    val event: SharedFlow<OnboardingEvent> = _event.asSharedFlow()

    fun updateCycleLength(length: Int) {
        _uiState.update { it.copy(cycleLength = length) }
    }

    fun updatePeriodLength(length: Int) {
        _uiState.update { it.copy(periodLength = length) }
    }
    
    fun updateHeight(height: Float?) {
        _uiState.update { it.copy(heightCm = height) }
    }
    
    fun updateWeight(weight: Float?) {
        _uiState.update { it.copy(weightKg = weight) }
    }
    
    fun updatePcosDiagnosis(hasPcos: Boolean?) {
        _uiState.update { it.copy(hasPolycysticOvaries = hasPcos) }
    }
    
    fun updateAcneSeverity(severity: String?) {
        _uiState.update { it.copy(acneSeverity = severity) }
    }
    
    fun updateHirsutismSeverity(severity: String?) {
        _uiState.update { it.copy(hirsutismSeverity = severity) }
    }

    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < 3) {
            _uiState.update { it.copy(currentStep = current + 1) }
        } else {
            completeOnboarding()
        }
    }
    
    fun prevStep() {
        val current = _uiState.value.currentStep
        if (current > 1) {
            _uiState.update { it.copy(currentStep = current - 1) }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            userPreferencesRepository.setDefaultCycleLength(state.cycleLength)
            userPreferencesRepository.setDefaultPeriodLength(state.periodLength)
            userPreferencesRepository.setHeightCm(state.heightCm)
            userPreferencesRepository.setWeightKg(state.weightKg)
            userPreferencesRepository.setHasPolycysticOvaries(state.hasPolycysticOvaries)
            userPreferencesRepository.setAcneSeverity(state.acneSeverity)
            userPreferencesRepository.setHirsutismSeverity(state.hirsutismSeverity)
            
            userPreferencesRepository.setOnboardingCompleted()
            
            _event.emit(OnboardingEvent.OnboardingFinished)
        }
    }
}
