package com.periodflow.core.domain.repository

import com.periodflow.core.domain.model.Cycle
import kotlinx.coroutines.flow.Flow

interface CycleRepository {
    fun getAllCycles(): Flow<List<Cycle>>
    fun getCompletedCycles(): Flow<List<Cycle>>
    suspend fun getCurrentCycle(): Cycle?
    suspend fun upsertCycle(cycle: Cycle)
    suspend fun deleteCycle(id: Long)
    fun getRecentCompletedCycles(count: Int): Flow<List<Cycle>>
}
