package com.periodflow.core.domain.model

import kotlinx.datetime.LocalDate

data class HealthAnalysisReport(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val indicators: List<HealthIndicator>,
    val recommendations: List<String>,
    val analysisDate: LocalDate,
    val cyclesAnalyzed: Int,
    val disclaimer: String = "This analysis is for informational purposes only. Please consult a healthcare professional for diagnosis."
)

enum class RiskLevel(val displayName: String, val emoji: String) {
    LOW("Low Risk", "🟢"),
    MODERATE("Moderate", "🟡"),
    ELEVATED("Elevated", "🟠"),
    HIGH("High Risk", "🔴")
}

data class HealthIndicator(
    val name: String,
    val description: String,
    val score: Int,
    val dataPoints: String
)
