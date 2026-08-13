package com.periodflow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodflow.core.common.today
import com.periodflow.core.domain.model.CyclePhase
import com.periodflow.core.domain.usecase.GetCurrentPhaseUseCase
import com.periodflow.core.domain.usecase.GetCycleHistoryUseCase
import com.periodflow.core.domain.usecase.PredictNextCycleUseCase
import com.periodflow.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import com.periodflow.core.health_analysis.DietRecommender
import com.periodflow.core.domain.repository.HealthAnalyzer

import androidx.compose.ui.graphics.vector.ImageVector
import com.periodflow.core.ui.components.icon

data class HomeUiState(
    val currentCycleDay: Int = 1,
    val phaseName: String = "Unknown",
    val phaseIcon: ImageVector? = null,
    val averageCycleLength: Int = 28,
    val averagePeriodLength: Int = 5,
    val totalCyclesLogged: Int = 0,
    val hasEnoughData: Boolean = false,
    val nextPeriodDateFormatted: String = "",
    val daysUntilNextPeriod: Int = 0,
    val fertileWindowFormatted: String = "",
    val confidenceLevel: String = "",
    val todayEpochDay: Long = LocalDate.today().toEpochDays().toLong(),
    val dailyDietTip: String = "",
    val companionMessage: String = "",
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentPhaseUseCase: GetCurrentPhaseUseCase,
    private val predictNextCycleUseCase: PredictNextCycleUseCase,
    private val getCycleHistoryUseCase: GetCycleHistoryUseCase,
    private val prefsRepository: UserPreferencesRepository,
    private val dietRecommender: DietRecommender,
    private val healthAnalyzer: HealthAnalyzer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                // Get current phase
                val (phase, dayInCycle) = getCurrentPhaseUseCase()
                
                // Get prediction
                val prediction = predictNextCycleUseCase()
                
                // Get prefs
                val prefs = prefsRepository.userPreferences.first()
                
                // Get cycle history count
                val cycles = getCycleHistoryUseCase().first()
                val completedCount = cycles.count { !it.isOngoing }
                
                // Get health analysis for diet adaptation
                val report = healthAnalyzer.analyzeHistory()
                // Fetch dynamic tip using OpenNutritionApi
                val dietTip = dietRecommender.fetchDynamicDietTip(
                    symptoms = emptyList(), // Pass recent symptoms if available
                    cycleDay = dayInCycle.coerceAtLeast(1),
                    phase = phase
                )

                val daysUntil = prediction?.nextPeriodStart?.let {
                    val today = LocalDate.today()
                    it.toEpochDays() - today.toEpochDays()
                } ?: 0

                val isPcos = prefs.hasPolycysticOvaries == true
                val companionMsg = when {
                    daysUntil < 0 && isPcos -> "Your period is late, which is common with PCOS. Try to manage stress and stay active today."
                    daysUntil < 0 -> "Your period is a bit late. Consider taking a test if you've been active, or just relax."
                    daysUntil in 0..2 -> "Your period is arriving soon. Keep a heat pad ready and stay hydrated!"
                    phase == CyclePhase.MENSTRUAL -> "You're on your period. Remember to log your flow and take it easy."
                    phase == CyclePhase.OVULATION -> "You might be ovulating! You may feel more energetic today."
                    isPcos -> "Managing PCOS is a journey. Keep logging your symptoms to build a reliable health profile."
                    else -> "Log your symptoms today to help PeriodFlow understand your cycle better."
                }

                _uiState.update { state ->
                    state.copy(
                        currentCycleDay = dayInCycle.coerceAtLeast(1),
                        phaseName = phase.displayName,
                        phaseIcon = phase.icon,
                        averageCycleLength = prediction?.averageCycleLength ?: prefs.defaultCycleLength,
                        averagePeriodLength = prediction?.averagePeriodLength ?: prefs.defaultPeriodLength,
                        totalCyclesLogged = completedCount,
                        hasEnoughData = prediction != null,
                        nextPeriodDateFormatted = prediction?.nextPeriodStart?.let { formatDate(it) } ?: "",
                        daysUntilNextPeriod = daysUntil.toInt(),
                        fertileWindowFormatted = prediction?.let {
                            "${formatDate(it.fertileWindowStart)} – ${formatDate(it.fertileWindowEnd)}"
                        } ?: "",
                        confidenceLevel = prediction?.confidence?.displayName ?: "",
                        dailyDietTip = dietTip,
                        companionMessage = companionMsg,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun formatDate(date: LocalDate): String {
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}"
    }
}
