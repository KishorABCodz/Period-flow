package com.periodflow.core.health_analysis

import com.periodflow.core.domain.model.HealthAnalysisReport
import com.periodflow.core.domain.model.HealthIndicator
import com.periodflow.core.domain.model.RiskLevel
import com.periodflow.core.domain.repository.CycleRepository
import com.periodflow.core.domain.repository.HealthAnalyzer
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import kotlin.math.abs

import com.periodflow.core.domain.repository.UserPreferencesRepository

class PcosRiskAnalyzer @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val cycleDayRepository: com.periodflow.core.domain.repository.CycleDayRepository,
    private val prefsRepository: UserPreferencesRepository
) : HealthAnalyzer {

    override suspend fun analyzeHistory(): HealthAnalysisReport? {
        val prefs = prefsRepository.userPreferences.firstOrNull()
        val cycles = cycleRepository.getCompletedCycles().firstOrNull() ?: return null
        if (cycles.isEmpty()) return null

        val cycleLengths = cycles.mapNotNull { it.cycleLength }
        if (cycleLengths.size < 3) return null // Need at least 3 cycles for meaningful analysis

        val averageLength = cycleLengths.average()
        var score = 0
        val indicators = mutableListOf<HealthIndicator>()

        // Indicator 1: Cycle Length
        val lengthScore = when {
            averageLength > 35 -> 30
            averageLength < 21 -> 15
            else -> 0
        }
        score += lengthScore
        indicators.add(
            HealthIndicator(
                name = "Average Cycle Length",
                description = "Typical cycle length should be between 21 and 35 days.",
                score = lengthScore,
                dataPoints = "Average: ${averageLength.toInt()} days"
            )
        )

        // Indicator 2: Cycle Variance
        val variance = cycleLengths.map { abs(it - averageLength) }.average()
        val varianceScore = when {
            variance > 7 -> 30
            variance > 4 -> 15
            else -> 0
        }
        score += varianceScore
        indicators.add(
            HealthIndicator(
                name = "Cycle Irregularity",
                description = "High variance in cycle lengths can indicate irregularity.",
                score = varianceScore,
                dataPoints = "Variance: ${variance.toInt()} days"
            )
        )
        
        // Indicator 3: Period Length
        val periodLengths = cycles.mapNotNull { it.periodLength }
        if (periodLengths.isNotEmpty()) {
            val avgPeriod = periodLengths.average()
            val periodScore = when {
                avgPeriod > 7 -> 20
                avgPeriod < 3 -> 10
                else -> 0
            }
            score += periodScore
            indicators.add(
                HealthIndicator(
                    name = "Period Duration",
                    description = "Typical bleeding lasts 3-7 days.",
                    score = periodScore,
                    dataPoints = "Average: ${avgPeriod.toInt()} days"
                )
            )
        }

        // Indicator 4: Hyperandrogenism Symptoms (Acne, Excess Hair, Hair Thinning)
        val cycleDays = cycleDayRepository.getAllDays().firstOrNull() ?: emptyList()
        val androgenSymptoms = listOf(
            com.periodflow.core.domain.model.Symptom.ACNE,
            com.periodflow.core.domain.model.Symptom.EXCESS_HAIR,
            com.periodflow.core.domain.model.Symptom.HAIR_THINNING
        )
        val reportedAndrogenSymptoms = cycleDays.flatMap { it.symptoms }.filter { it in androgenSymptoms }
        val androgenScore = when {
            reportedAndrogenSymptoms.size >= 5 -> 30
            reportedAndrogenSymptoms.size >= 2 -> 15
            else -> 0
        }
        score += androgenScore
        if (androgenScore > 0) {
            indicators.add(
                HealthIndicator(
                    name = "Androgen-related Symptoms",
                    description = "Symptoms like severe acne or unusual hair changes can be related to hormonal imbalances like PCOS.",
                    score = androgenScore,
                    dataPoints = "Reported ${reportedAndrogenSymptoms.size} times"
                )
            )
        }

        // Indicator 5: Ultrasound / Clinical Diagnosis of Polycystic Ovaries
        if (prefs?.hasPolycysticOvaries == true) {
            score += 40
            indicators.add(
                HealthIndicator(
                    name = "Polycystic Ovaries",
                    description = "Ultrasound-confirmed polycystic ovaries are a primary diagnostic criteria.",
                    score = 40,
                    dataPoints = "Confirmed by user"
                )
            )
        }

        // Indicator 6: BMI & Insulin Resistance
        val latestWeight = cycleDays.mapNotNull { it.weightKg }.lastOrNull()
        val heightCm = prefs?.heightCm
        if (latestWeight != null && heightCm != null) {
            val heightM = heightCm / 100f
            val bmi = latestWeight / (heightM * heightM)
            if (bmi >= 25f) {
                val bmiScore = if (bmi >= 30f) 20 else 10
                score += bmiScore
                indicators.add(
                    HealthIndicator(
                        name = "BMI (Insulin Resistance Risk)",
                        description = "Elevated BMI is strongly correlated with insulin resistance in PCOS.",
                        score = bmiScore,
                        dataPoints = "BMI: ${String.format("%.1f", bmi)}"
                    )
                )
            }
        }

        val riskLevel = when {
            score >= 60 -> RiskLevel.HIGH
            score >= 40 -> RiskLevel.ELEVATED
            score >= 20 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        val recommendations = mutableListOf<String>()
        if (riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.ELEVATED) {
            recommendations.add("Consider consulting a healthcare provider about irregular cycles.")
        }
        if (lengthScore > 0) {
            recommendations.add("Track your cycles carefully, as they fall outside the typical 21-35 day range.")
        }

        return HealthAnalysisReport(
            riskScore = score,
            riskLevel = riskLevel,
            indicators = indicators,
            recommendations = recommendations,
            analysisDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            cyclesAnalyzed = cycles.size
        )
    }

    override fun getIndicatorDetails(indicator: HealthIndicator): String {
        return "${indicator.name}\n${indicator.description}\nValue: ${indicator.dataPoints}"
    }
}
