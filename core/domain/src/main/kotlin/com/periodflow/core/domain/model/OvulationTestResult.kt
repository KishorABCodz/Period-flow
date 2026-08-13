package com.periodflow.core.domain.model

enum class OvulationTestResult(val displayName: String) {
    POSITIVE("Positive (Surge Detected)"),
    NEGATIVE("Negative");
    
    companion object {
        fun fromName(name: String?): OvulationTestResult? = 
            if (name == null) null else entries.find { it.name == name }
    }
}
