package com.periodflow.feature.health_insights.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.periodflow.core.ai.model.AiStreamEvent
import com.periodflow.core.ai.repository.GeminiAiRepository
import com.periodflow.core.domain.repository.AiInsightCache
import com.periodflow.core.domain.repository.CycleRepository
import com.periodflow.core.domain.repository.HealthAnalyzer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Nightly worker that refreshes the cached AI insight so users see fresh
 * personalised text every morning.
 *
 * Zero-hallucination behaviour:
 * - No-ops when there isn't enough data (analyzer returns null).
 * - Silently succeeds when the API key isn't configured or the stream fails,
 *   so we never crash a periodic worker for a background feature.
 */
@HiltWorker
class NightlyInsightWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val healthAnalyzer: HealthAnalyzer,
    private val cycleRepository: CycleRepository,
    private val geminiAiRepository: GeminiAiRepository,
    private val aiInsightCache: AiInsightCache,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val report = healthAnalyzer.analyzeHistory() ?: return Result.success()

            val cycles = runCatching { cycleRepository.getAllCycles().first() }
                .getOrDefault(emptyList())
            val avgCycle = cycles.mapNotNull { it.cycleLength }
                .takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
            val avgPeriod = cycles.mapNotNull { it.periodLength }
                .takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0

            val buffer = StringBuilder()
            var failed = false
            geminiAiRepository.streamInsightNarrative(
                report = report,
                averageCycleLength = avgCycle,
                averagePeriodLength = avgPeriod,
                totalCycles = cycles.size,
            ).collect { event ->
                when (event) {
                    is AiStreamEvent.Delta -> buffer.append(event.text)
                    AiStreamEvent.Done -> Unit
                    is AiStreamEvent.Error -> {
                        failed = true
                    }
                }
            }

            val text = buffer.toString().trim()
            if (!failed && text.isNotBlank()) {
                aiInsightCache.save(text, report.riskScore)
            }
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val UNIQUE_NAME = "nightly_insight_refresh"
    }
}
