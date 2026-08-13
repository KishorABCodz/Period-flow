package com.periodflow.core.domain.model

import kotlinx.datetime.LocalDate

data class CyclePrediction(
    val nextPeriodStart: LocalDate,
    val nextPeriodEnd: LocalDate,
    val fertileWindowStart: LocalDate,
    val fertileWindowEnd: LocalDate,
    val ovulationDate: LocalDate,
    val confidence: PredictionConfidence,
    val averageCycleLength: Int,
    val averagePeriodLength: Int,
)
