package com.periodflow.core.database.mapper

import com.periodflow.core.common.toEpochDay
import com.periodflow.core.common.toLocalDate
import com.periodflow.core.database.entity.CycleEntity
import com.periodflow.core.domain.model.Cycle

fun CycleEntity.toDomain(): Cycle {
    return Cycle(
        id = id,
        startDate = startDateEpochDay.toLocalDate(),
        endDate = endDateEpochDay?.toLocalDate(),
        periodLength = periodLength,
        cycleLength = cycleLength,
    )
}

fun Cycle.toEntity(): CycleEntity {
    return CycleEntity(
        id = id,
        startDateEpochDay = startDate.toEpochDay(),
        endDateEpochDay = endDate?.toEpochDay(),
        periodLength = periodLength,
        cycleLength = cycleLength,
    )
}
