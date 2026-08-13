package com.periodflow.core.domain.model

enum class FlowIntensity(val displayName: String) {
    NONE("None"),
    SPOTTING("Spotting"),
    LIGHT("Light"),
    MEDIUM("Medium"),
    HEAVY("Heavy");

    companion object {
        fun fromName(name: String?): FlowIntensity? =
            if (name == null) null else entries.find { it.name == name }
    }
}
