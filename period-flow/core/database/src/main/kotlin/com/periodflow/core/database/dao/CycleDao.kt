package com.periodflow.core.database.dao

import androidx.room.*
import com.periodflow.core.database.entity.CycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {

    @Query("SELECT * FROM cycles ORDER BY startDateEpochDay DESC")
    fun getAllCycles(): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycles WHERE endDateEpochDay IS NOT NULL ORDER BY startDateEpochDay DESC")
    fun getCompletedCycles(): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycles WHERE endDateEpochDay IS NULL ORDER BY startDateEpochDay DESC LIMIT 1")
    suspend fun getCurrentCycle(): CycleEntity?

    @Upsert
    suspend fun upsertCycle(cycle: CycleEntity)

    @Query("DELETE FROM cycles WHERE id = :id")
    suspend fun deleteCycle(id: Long)

    @Query("SELECT * FROM cycles WHERE endDateEpochDay IS NOT NULL AND cycleLength IS NOT NULL ORDER BY startDateEpochDay DESC LIMIT :count")
    fun getRecentCompletedCycles(count: Int): Flow<List<CycleEntity>>
}
