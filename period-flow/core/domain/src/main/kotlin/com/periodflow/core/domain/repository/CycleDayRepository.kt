package com.periodflow.core.domain.repository

import com.periodflow.core.domain.model.CycleDay
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface CycleDayRepository {
    fun getDaysInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<CycleDay>>
    suspend fun getDayByDate(date: LocalDate): CycleDay?
    suspend fun upsertDay(day: CycleDay)
    suspend fun deleteDay(date: LocalDate)
    fun getAllDays(): Flow<List<CycleDay>>
}
