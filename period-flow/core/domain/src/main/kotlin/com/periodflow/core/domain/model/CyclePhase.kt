package com.periodflow.core.domain.model

enum class CyclePhase(
    val displayName: String,
    val description: String,
) {
    MENSTRUAL(
        displayName = "Menstrual",
        description = "Period phase — your body is shedding the uterine lining",
    ),
    FOLLICULAR(
        displayName = "Follicular",
        description = "Pre-ovulation — estrogen rises, energy increases",
    ),
    OVULATION(
        displayName = "Ovulation",
        description = "Fertile window — egg is released",
    ),
    LUTEAL(
        displayName = "Luteal",
        description = "Post-ovulation — progesterone rises, PMS may occur",
    ),
    UNKNOWN(
        displayName = "Unknown",
        description = "Not enough data to determine phase",
    );
}
