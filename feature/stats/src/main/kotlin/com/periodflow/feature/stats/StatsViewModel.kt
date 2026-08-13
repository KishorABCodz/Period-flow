package com.periodflow.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodflow.core.domain.model.Cycle
import com.periodflow.core.domain.usecase.GetCycleHistoryUseCase
import com.periodflow.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import android.net.Uri
import com.periodflow.core.domain.repository.ExportResult
import com.periodflow.core.domain.repository.HealthAnalyzer
import com.periodflow.core.domain.repository.ReportExporter

data class CycleUiModel(
    val id: Long,
    val dateRange: String,
    val cycleLength: Int?,
    val periodLength: Int?,
    val isOngoing: Boolean,
)

data class StatsUiState(
    val averageCycleLength: Int = 28,
    val averagePeriodLength: Int = 5,
    val cycleLengths: List<Int> = emptyList(),
    val cycles: List<CycleUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isGeneratingReport: Boolean = false,
    val reportUri: Uri? = null,
    val reportError: String? = null,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getCycleHistoryUseCase: GetCycleHistoryUseCase,
    private val prefsRepository: UserPreferencesRepository,
    private val healthAnalyzer: HealthAnalyzer,
    private val reportExporter: ReportExporter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCycleHistoryUseCase().combine(
                prefsRepository.userPreferences
            ) { cycles, prefs ->
                val completedCycles = cycles.filter { !it.isOngoing }
                val cycleLengths = completedCycles.mapNotNull { it.cycleLength }
                val periodLengths = completedCycles.mapNotNull { it.periodLength }

                StatsUiState(
                    averageCycleLength = if (cycleLengths.isNotEmpty()) cycleLengths.average().toInt() else prefs.defaultCycleLength,
                    averagePeriodLength = if (periodLengths.isNotEmpty()) periodLengths.average().toInt() else prefs.defaultPeriodLength,
                    cycleLengths = cycleLengths.takeLast(10),
                    cycles = cycles.map { it.toUiModel() },
                    isLoading = false,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun Cycle.toUiModel(): CycleUiModel {
        return CycleUiModel(
            id = id,
            dateRange = "${formatDate(startDate)}${endDate?.let { " – ${formatDate(it)}" } ?: " – present"}",
            cycleLength = cycleLength,
            periodLength = periodLength,
            isOngoing = isOngoing,
        )
    }

    private fun formatDate(date: LocalDate): String {
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}"
    }

    fun generateReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingReport = true, reportError = null, reportUri = null) }
            val cycles = getCycleHistoryUseCase().first()
            val analysis = healthAnalyzer.analyzeHistory()
            val result = reportExporter.generatePdfReport(cycles, emptyList(), analysis)
            
            when (result) {
                is ExportResult.Success -> {
                    _uiState.update { it.copy(isGeneratingReport = false, reportUri = result.fileUri) }
                }
                is ExportResult.Error -> {
                    _uiState.update { it.copy(isGeneratingReport = false, reportError = result.message) }
                }
            }
        }
    }
    
    fun dismissReportMessage() {
        _uiState.update { it.copy(reportUri = null, reportError = null) }
    }
}
