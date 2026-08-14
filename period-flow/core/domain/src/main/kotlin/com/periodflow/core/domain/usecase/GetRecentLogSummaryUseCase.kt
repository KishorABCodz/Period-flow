package com.periodflow.core.domain.usecase

import com.periodflow.core.common.today
import com.periodflow.core.domain.model.FlowIntensity
import com.periodflow.core.domain.model.Mood
import com.periodflow.core.domain.model.RecentLogSummary
import com.periodflow.core.domain.model.Symptom
import com.periodflow.core.domain.repository.CycleDayRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject

/**
 * Aggregates the most recent [windowDays] days of logs into a compact,
 * non-identifiable summary suitable for AI prompts.
 *
 * Only counts and top-N modes — never raw dates, weights, or notes verbatim.
 */
class GetRecentLogSummaryUseCase @Inject constructor(
    private val cycleDayRepository: CycleDayRepository,
) {
    suspend operator fun invoke(windowDays: Int = 7): RecentLogSummary {
        val today = LocalDate.today()
        val start = today.minus(DatePeriod(days = windowDays - 1))
        val days = cycleDayRepository.getDaysInRange(start, today).first()

        val symptomCounts = mutableMapOf<Symptom, Int>()
        val moodCounts = mutableMapOf<Mood, Int>()
        var strongestFlow: FlowIntensity? = null
        val notesBuilder = StringBuilder()

        days.forEach { d ->
            d.symptoms.forEach { s -> symptomCounts[s] = (symptomCounts[s] ?: 0) + 1 }
            d.mood?.let { m -> moodCounts[m] = (moodCounts[m] ?: 0) + 1 }
            d.flowIntensity?.let { f ->
                if (strongestFlow == null || f.ordinal > (strongestFlow?.ordinal ?: -1)) {
                    strongestFlow = f
                }
            }
            if (d.notes.isNotBlank() && notesBuilder.length < 200) {
                if (notesBuilder.isNotEmpty()) notesBuilder.append(" | ")
                notesBuilder.append(d.notes.take(60))
            }
        }

        val topSymptoms = symptomCounts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val dominantMood = moodCounts.entries.maxByOrNull { it.value }?.key

        return RecentLogSummary(
            windowDays = windowDays,
            daysLogged = days.count { d ->
                d.flowIntensity != null || d.mood != null || d.symptoms.isNotEmpty() || d.notes.isNotBlank()
            },
            topSymptoms = topSymptoms,
            dominantMood = dominantMood,
            strongestFlow = strongestFlow,
            notesDigest = notesBuilder.toString(),
        )
    }
}
