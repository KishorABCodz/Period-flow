package com.periodflow.feature.health_insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodflow.core.domain.model.HealthAnalysisReport
import com.periodflow.core.domain.repository.CycleDayRepository
import com.periodflow.core.domain.repository.CycleRepository
import com.periodflow.core.domain.repository.ExportResult
import com.periodflow.core.domain.repository.HealthAnalyzer
import com.periodflow.core.domain.repository.ReportExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HealthInsightsState {
    data object Loading : HealthInsightsState
    data class Success(val report: HealthAnalysisReport) : HealthInsightsState
    data class Error(val message: String) : HealthInsightsState
}

sealed interface ExportState {
    data object Idle : ExportState
    data object Exporting : ExportState
    data class Success(val uri: android.net.Uri) : ExportState
    data class Error(val message: String) : ExportState
}

@HiltViewModel
class HealthInsightsViewModel @Inject constructor(
    private val healthAnalyzer: HealthAnalyzer,
    private val reportExporter: ReportExporter,
    private val cycleRepository: CycleRepository,
    private val cycleDayRepository: CycleDayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthInsightsState>(HealthInsightsState.Loading)
    val uiState: StateFlow<HealthInsightsState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    init {
        analyzeHealth()
    }

    fun analyzeHealth() {
        viewModelScope.launch {
            _uiState.value = HealthInsightsState.Loading
            try {
                val report = healthAnalyzer.analyzeHistory()
                if (report != null) {
                    _uiState.value = HealthInsightsState.Success(report)
                } else {
                    _uiState.value = HealthInsightsState.Error("Not enough data for analysis.")
                }
            } catch (e: Exception) {
                _uiState.value = HealthInsightsState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HealthInsightsState.Success) {
                _exportState.value = ExportState.Exporting
                try {
                    val cycles = cycleRepository.getAllCycles().first()
                    val days = cycleDayRepository.getAllDays().first()
                    
                    val result = reportExporter.generatePdfReport(cycles, days, currentState.report)
                    _exportState.value = when (result) {
                        is ExportResult.Success -> ExportState.Success(result.fileUri)
                        is ExportResult.Error -> ExportState.Error(result.message)
                    }
                } catch (e: Exception) {
                    _exportState.value = ExportState.Error(e.message ?: "Export failed")
                }
            }
        }
    }
    
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}
