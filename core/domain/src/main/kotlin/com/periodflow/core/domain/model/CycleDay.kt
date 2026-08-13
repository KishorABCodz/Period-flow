package com.periodflow.core.domain.model

import kotlinx.datetime.LocalDate

data class CycleDay(
    val id: Long = 0,
    val date: LocalDate,
    val flowIntensity: FlowIntensity? = null,
    val symptoms: List<Symptom> = emptyList(),
    val mood: Mood? = null,
    val notes: String = "",
    val temperature: Float? = null,
    val weightKg: Float? = null,
    val ovulationTestResult: OvulationTestResult? = null,
) {
    val hasFlow: Boolean get() = flowIntensity != null && flowIntensity != FlowIntensity.NONE
    val hasPeriod: Boolean get() = flowIntensity != null && flowIntensity != FlowIntensity.NONE && flowIntensity != FlowIntensity.SPOTTING
}
