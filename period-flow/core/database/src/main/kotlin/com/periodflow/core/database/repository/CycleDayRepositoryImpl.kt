package com.periodflow.core.database.repository

import com.periodflow.core.common.toEpochDay
import com.periodflow.core.database.dao.CycleDayDao
import com.periodflow.core.database.mapper.toDomain
import com.periodflow.core.database.mapper.toEntity
import com.periodflow.core.domain.model.CycleDay
import com.periodflow.core.domain.repository.CycleDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class CycleDayRepositoryImpl @Inject constructor(
    private val cycleDayDao: CycleDayDao,
) : CycleDayRepository {

    override fun getDaysInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<CycleDay>> {
        return cycleDayDao.getDaysInRange(
            startEpoch = startDate.toEpochDay(),
            endEpoch = endDate.toEpochDay(),
        ).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getDayByDate(date: LocalDate): CycleDay? {
        return cycleDayDao.getDayByDate(date.toEpochDay())?.toDomain()
    }

    override suspend fun upsertDay(day: CycleDay) {
        // Check if a day already exists for this date
        val existing = cycleDayDao.getDayByDate(day.date.toEpochDay())
        val entity = if (existing != null) {
            day.copy(id = existing.id).toEntity()
        } else {
            day.toEntity()
        }
        cycleDayDao.upsertDay(entity)
    }

    override suspend fun deleteDay(date: LocalDate) {
        cycleDayDao.deleteByDate(date.toEpochDay())
    }

    override fun getAllDays(): Flow<List<CycleDay>> {
        return cycleDayDao.getAllDays().map { entities -> entities.map { it.toDomain() } }
    }
}
