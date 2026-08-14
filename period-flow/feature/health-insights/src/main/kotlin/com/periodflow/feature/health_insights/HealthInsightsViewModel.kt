package com.periodflow.feature.health_insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodflow.core.ai.model.AiResult
import com.periodflow.core.ai.model.AiStreamEvent
import com.periodflow.core.ai.repository.GeminiAiRepository
import com.periodflow.core.domain.model.HealthAnalysisReport
import com.periodflow.core.domain.repository.AiInsightCache
import com.periodflow.core.domain.repository.CycleDayRepository
import com.periodflow.core.domain.repository.CycleRepository
import com.periodflow.core.domain.repository.ExportResult
import com.periodflow.core.domain.repository.HealthAnalyzer
import com.periodflow.core.domain.repository.ReportExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val cycleDayRepository: CycleDayRepository,
    private val geminiAiRepository: GeminiAiRepository,
    private val aiInsightCache: AiInsightCache,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthInsightsState>(HealthInsightsState.Loading)
    val uiState: StateFlow<HealthInsightsState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    /**
     * Streaming AI narrative state.
     * `AiResult.Success` is set when the stream ends (or when a cached value is loaded).
     * During streaming, `AiResult.Loading` is used and `streamedText` grows word by word.
     */
    private val _aiNarrative = MutableStateFlow<AiResult<String>>(AiResult.Idle)
    val aiNarrative: StateFlow<AiResult<String>> = _aiNarrative.asStateFlow()

    private val _streamedText = MutableStateFlow("")
    val streamedText: StateFlow<String> = _streamedText.asStateFlow()

    /** True until we've hydrated from Room; the UI can show a subtle "refreshing" hint. */
    private val _isFromCache = MutableStateFlow(false)
    val isFromCache: StateFlow<Boolean> = _isFromCache.asStateFlow()

    private var streamJob: Job? = null

    init {
        hydrateFromCache()
        analyzeHealth()
    }

    private fun hydrateFromCache() {
        viewModelScope.launch {
            val cached = aiInsightCache.getOnce()
            if (cached != null && _aiNarrative.value !is AiResult.Success) {
                _streamedText.value = cached.narrative
                _aiNarrative.value = AiResult.Success(cached.narrative)
                _isFromCache.value = true
            }
        }
    }

    fun analyzeHealth() {
        viewModelScope.launch {
            _uiState.value = HealthInsightsState.Loading
            try {
                val report = healthAnalyzer.analyzeHistory()
                if (report != null) {
                    _uiState.value = HealthInsightsState.Success(report)
                    // Cache invalidation: if the underlying deterministic risk score changed,
                    // discard the cached narrative so users don't see stale personalised text.
                    val cached = aiInsightCache.getOnce()
                    if (cached != null && cached.basedOnRiskScore != report.riskScore) {
                        aiInsightCache.clear()
                        _streamedText.value = ""
                        _aiNarrative.value = AiResult.Loading
                        _isFromCache.value = false
                    }
                    startNarrativeStream(report)
                } else {
                    _uiState.value = HealthInsightsState.Error("Not enough data for analysis.")
                }
            } catch (e: Exception) {
                _uiState.value = HealthInsightsState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun startNarrativeStream(report: HealthAnalysisReport) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            _isFromCache.value = _aiNarrative.value is AiResult.Success // still show cached while refreshing
            // Only reset the UI narrative when we don't have any cached text.
            if (_streamedText.value.isBlank()) {
                _aiNarrative.value = AiResult.Loading
            }

            val cycles = runCatching { cycleRepository.getAllCycles().first() }.getOrDefault(emptyList())
            val avgCycle = cycles.mapNotNull { it.cycleLength }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
            val avgPeriod = cycles.mapNotNull { it.periodLength }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0

            val buffer = StringBuilder()
            geminiAiRepository.streamInsightNarrative(
                report = report,
                averageCycleLength = avgCycle,
                averagePeriodLength = avgPeriod,
                totalCycles = cycles.size,
            ).collect { event ->
                when (event) {
                    is AiStreamEvent.Delta -> {
                        // On the first delta, clear the cached echo so the UI shows fresh tokens.
                        if (buffer.isEmpty()) _streamedText.value = ""
                        buffer.append(event.text)
                        _streamedText.value = buffer.toString()
                        _aiNarrative.value = AiResult.Loading
                    }
                    AiStreamEvent.Done -> {
                        val finalText = buffer.toString().trim()
                        if (finalText.isNotBlank()) {
                            _aiNarrative.value = AiResult.Success(finalText)
                            _isFromCache.value = false
                            aiInsightCache.save(finalText, report.riskScore)
                        } else if (_streamedText.value.isBlank()) {
                            _aiNarrative.value = AiResult.Error("Empty response from Gemini.")
                        }
                    }
                    is AiStreamEvent.Error -> {
                        // Fall back to cached text if available, otherwise surface the error.
                        if (_streamedText.value.isNotBlank()) {
                            _aiNarrative.value = AiResult.Success(_streamedText.value)
                        } else {
                            _aiNarrative.value = AiResult.Error(event.message)
                        }
                    }
                }
            }
        }
    }

    fun retryAiNarrative() {
        val state = _uiState.value
        if (state is HealthInsightsState.Success) startNarrativeStream(state.report)
    }

    fun exportPdf() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HealthInsightsState.Success) {
                _exportState.value = ExportState.Exporting
                try {
                    val cycles = cycleRepository.getAllCycles().first()
                    val days = cycleDayRepository.getAllDays().first()
                    val narrative = (_aiNarrative.value as? AiResult.Success)?.value
                        ?: _streamedText.value.takeIf { it.isNotBlank() }

                    val result = reportExporter.generatePdfReport(
                        cycles = cycles,
                        days = days,
                        analysis = currentState.report,
                        aiNarrative = narrative,
                    )
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
