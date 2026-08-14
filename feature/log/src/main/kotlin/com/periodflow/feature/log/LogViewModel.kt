package com.periodflow.feature.log

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodflow.core.ai.model.AiResult
import com.periodflow.core.ai.repository.GeminiAiRepository
import com.periodflow.core.common.toLocalDate
import com.periodflow.core.domain.model.*
import com.periodflow.core.domain.usecase.GetCurrentPhaseUseCase
import com.periodflow.core.domain.usecase.LogCycleDayUseCase
import com.periodflow.core.domain.repository.CycleDayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class LogUiState(
    val dateFormatted: String = "",
    val dateEpochDay: Long = 0,
    val selectedFlow: FlowIntensity? = null,
    val selectedMood: Mood? = null,
    val selectedSymptoms: Set<Symptom> = emptySet(),
    val notes: String = "",
    val weightKg: String = "",
    val ovulationTestResult: OvulationTestResult? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    // Symptom explainer (AI)
    val explainerOpenFor: Symptom? = null,
    val explainerResult: AiResult<String> = AiResult.Idle,
)

@HiltViewModel
class LogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logCycleDayUseCase: LogCycleDayUseCase,
    private val cycleDayRepository: CycleDayRepository,
    private val getCurrentPhaseUseCase: GetCurrentPhaseUseCase,
    private val geminiAiRepository: GeminiAiRepository,
) : ViewModel() {

    private val dateEpochDay: Long = savedStateHandle.get<Long>("dateEpochDay") ?: 0L

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        loadExistingDay()
    }

    private fun loadExistingDay() {
        viewModelScope.launch {
            val date = dateEpochDay.toLocalDate()
            val formatted = formatDate(date)
            
            val existing = cycleDayRepository.getDayByDate(date)
            
            _uiState.update {
                it.copy(
                    dateFormatted = formatted,
                    dateEpochDay = dateEpochDay,
                    selectedFlow = existing?.flowIntensity,
                    selectedMood = existing?.mood,
                    selectedSymptoms = existing?.symptoms?.toSet() ?: emptySet(),
                    notes = existing?.notes ?: "",
                    weightKg = existing?.weightKg?.toString() ?: "",
                    ovulationTestResult = existing?.ovulationTestResult,
                )
            }
        }
    }

    fun onFlowSelected(flow: FlowIntensity) {
        _uiState.update {
            it.copy(selectedFlow = if (it.selectedFlow == flow) null else flow)
        }
    }

    fun onMoodSelected(mood: Mood) {
        _uiState.update {
            it.copy(selectedMood = if (it.selectedMood == mood) null else mood)
        }
    }

    fun onSymptomToggled(symptom: Symptom) {
        _uiState.update {
            val updated = it.selectedSymptoms.toMutableSet()
            if (symptom in updated) updated.remove(symptom) else updated.add(symptom)
            it.copy(selectedSymptoms = updated)
        }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onWeightChanged(weight: String) {
        // Only allow numbers and one decimal point
        if (weight.isEmpty() || weight.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(weightKg = weight) }
        }
    }

    fun onOvulationTestResultSelected(result: OvulationTestResult) {
        _uiState.update { 
            it.copy(ovulationTestResult = if (it.ovulationTestResult == result) null else result) 
        }
    }

    fun openSymptomExplainer(symptom: Symptom) {
        _uiState.update { it.copy(explainerOpenFor = symptom, explainerResult = AiResult.Loading) }
        viewModelScope.launch {
            val (phase, _) = runCatching { getCurrentPhaseUseCase() }
                .getOrDefault(CyclePhase.UNKNOWN to 0)
            val result = geminiAiRepository.explainSymptom(symptom, phase)
            _uiState.update { it.copy(explainerResult = result) }
        }
    }

    fun closeSymptomExplainer() {
        _uiState.update { it.copy(explainerOpenFor = null, explainerResult = AiResult.Idle) }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val day = CycleDay(
                date = dateEpochDay.toLocalDate(),
                flowIntensity = _uiState.value.selectedFlow,
                mood = _uiState.value.selectedMood,
                symptoms = _uiState.value.selectedSymptoms.toList(),
                notes = _uiState.value.notes,
                weightKg = _uiState.value.weightKg.toFloatOrNull(),
                ovulationTestResult = _uiState.value.ovulationTestResult,
            )
            
            logCycleDayUseCase(day)
            _uiState.update { it.copy(isSaved = true, isLoading = false) }
        }
    }

    private fun formatDate(date: LocalDate): String {
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
    }
}
