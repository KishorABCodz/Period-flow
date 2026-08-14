package com.periodflow.core.domain.model

import kotlinx.datetime.LocalDate

data class Cycle(
    val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val periodLength: Int? = null,
    val cycleLength: Int? = null,
) {
    val isOngoing: Boolean get() = endDate == null
}
