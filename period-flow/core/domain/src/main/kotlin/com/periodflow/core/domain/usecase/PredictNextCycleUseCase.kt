package com.periodflow.core.domain.usecase

import com.periodflow.core.common.plusDays
import com.periodflow.core.common.minusDays
import com.periodflow.core.common.toLocalDate
import com.periodflow.core.domain.model.Cycle
import com.periodflow.core.domain.model.CyclePrediction
import com.periodflow.core.domain.model.PredictionConfidence
import com.periodflow.core.domain.repository.CycleRepository
import com.periodflow.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PredictNextCycleUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val prefsRepository: UserPreferencesRepository,
) {
    /**
     * Predicts next period using Simple Moving Average of last N completed cycles.
     *
     * Algorithm:
     * 1. Gather last 6 completed cycles (or fewer if not enough data)
     * 2. Calculate average cycle length and average period length
     * 3. Next period start = last period start + average cycle length
     * 4. Ovulation = next period start - 14 days (luteal phase constant)
     * 5. Fertile window = ovulation ± 5 days
     *
     * Confidence:
     * - < 3 completed cycles → LOW (uses user-provided defaults)
     * - 3–5 completed cycles → MEDIUM
     * - 6+ completed cycles → HIGH
     */
    suspend operator fun invoke(): CyclePrediction? {
        val completedCycles = cycleRepository.getRecentCompletedCycles(6).first()
        val prefs = prefsRepository.userPreferences.first()

        val lastCycleStart = completedCycles.firstOrNull()?.startDate
            ?: prefs.lastPeriodStartEpochDay?.let {
                it.toLocalDate()
            }
            ?: return null // No data at all

        val (avgCycleLength, avgPeriodLength, confidence) = when {
            completedCycles.size >= 6 -> Triple(
                completedCycles.mapNotNull { it.cycleLength }.average().toInt(),
                completedCycles.mapNotNull { it.periodLength }.average().toInt(),
                PredictionConfidence.HIGH,
            )
            completedCycles.size >= 3 -> Triple(
                completedCycles.mapNotNull { it.cycleLength }.average().toInt(),
                completedCycles.mapNotNull { it.periodLength }.average().toInt(),
                PredictionConfidence.MEDIUM,
            )
            else -> Triple(
                prefs.defaultCycleLength,
                prefs.defaultPeriodLength,
                PredictionConfidence.LOW,
            )
        }

        val nextPeriodStart = lastCycleStart.plusDays(avgCycleLength)
        val nextPeriodEnd = nextPeriodStart.plusDays(avgPeriodLength - 1)
        val ovulationDate = nextPeriodStart.minusDays(14)
        val fertileWindowStart = ovulationDate.minusDays(5)
        val fertileWindowEnd = ovulationDate.plusDays(1)

        return CyclePrediction(
            nextPeriodStart = nextPeriodStart,
            nextPeriodEnd = nextPeriodEnd,
            fertileWindowStart = fertileWindowStart,
            fertileWindowEnd = fertileWindowEnd,
            ovulationDate = ovulationDate,
            confidence = confidence,
            averageCycleLength = avgCycleLength,
            averagePeriodLength = avgPeriodLength,
        )
    }
}
