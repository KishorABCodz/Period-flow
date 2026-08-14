package com.periodflow.core.domain.model

enum class PredictionConfidence(val displayName: String, val description: String) {
    LOW("Low", "Less than 3 recorded cycles — using your defaults"),
    MEDIUM("Medium", "3–5 recorded cycles — predictions improving"),
    HIGH("High", "6+ recorded cycles — reliable predictions");
}
