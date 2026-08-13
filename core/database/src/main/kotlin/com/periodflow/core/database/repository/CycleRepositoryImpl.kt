package com.periodflow.core.database.repository

import com.periodflow.core.database.dao.CycleDao
import com.periodflow.core.database.mapper.toDomain
import com.periodflow.core.database.mapper.toEntity
import com.periodflow.core.domain.model.Cycle
import com.periodflow.core.domain.repository.CycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CycleRepositoryImpl @Inject constructor(
    private val cycleDao: CycleDao,
) : CycleRepository {

    override fun getAllCycles(): Flow<List<Cycle>> {
        return cycleDao.getAllCycles().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getCompletedCycles(): Flow<List<Cycle>> {
        return cycleDao.getCompletedCycles().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getCurrentCycle(): Cycle? {
        return cycleDao.getCurrentCycle()?.toDomain()
    }

    override suspend fun upsertCycle(cycle: Cycle) {
        cycleDao.upsertCycle(cycle.toEntity())
    }

    override suspend fun deleteCycle(id: Long) {
        cycleDao.deleteCycle(id)
    }

    override fun getRecentCompletedCycles(count: Int): Flow<List<Cycle>> {
        return cycleDao.getRecentCompletedCycles(count)
            .map { entities -> entities.map { it.toDomain() } }
    }
}
