package com.periodflow.core.domain.model

enum class Symptom(val displayName: String) {
    CRAMPS("Cramps"),
    HEADACHE("Headache"),
    BACKACHE("Backache"),
    BLOATING("Bloating"),
    BREAST_TENDERNESS("Breast Tenderness"),
    ACNE("Acne"),
    FATIGUE("Fatigue"),
    NAUSEA("Nausea"),
    INSOMNIA("Insomnia"),
    CRAVINGS("Cravings"),
    DIARRHEA("Diarrhea"),
    CONSTIPATION("Constipation"),
    HOT_FLASHES("Hot Flashes"),
    DIZZINESS("Dizziness"),
    EXCESS_HAIR("Excess Hair"),
    HAIR_THINNING("Hair Thinning"),
    PELVIC_PAIN("Pelvic Pain"),
    SKIN_DARKENING("Skin Darkening");

    companion object {
        fun fromName(name: String): Symptom? =
            entries.find { it.name == name }
    }
}
