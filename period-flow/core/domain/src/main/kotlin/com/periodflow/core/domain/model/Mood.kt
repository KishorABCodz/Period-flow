package com.periodflow.core.domain.model

enum class Mood(val displayName: String) {
    HAPPY("Happy"),
    CALM("Calm"),
    SENSITIVE("Sensitive"),
    SAD("Sad"),
    ANXIOUS("Anxious"),
    IRRITABLE("Irritable"),
    ENERGETIC("Energetic"),
    TIRED("Tired");

    companion object {
        fun fromName(name: String?): Mood? =
            if (name == null) null else entries.find { it.name == name }
    }
}
