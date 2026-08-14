package com.periodflow.core.database.dao

import androidx.room.*
import com.periodflow.core.database.entity.CycleDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDayDao {

    @Query("SELECT * FROM cycle_days WHERE dateEpochDay BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpochDay ASC")
    fun getDaysInRange(startEpoch: Long, endEpoch: Long): Flow<List<CycleDayEntity>>

    @Query("SELECT * FROM cycle_days WHERE dateEpochDay = :epochDay LIMIT 1")
    suspend fun getDayByDate(epochDay: Long): CycleDayEntity?

    @Upsert
    suspend fun upsertDay(day: CycleDayEntity)

    @Query("DELETE FROM cycle_days WHERE dateEpochDay = :epochDay")
    suspend fun deleteByDate(epochDay: Long)

    @Query("SELECT * FROM cycle_days ORDER BY dateEpochDay ASC")
    fun getAllDays(): Flow<List<CycleDayEntity>>

    @Query("SELECT * FROM cycle_days WHERE flowIntensity IS NOT NULL AND flowIntensity != 'NONE' ORDER BY dateEpochDay DESC")
    fun getFlowDays(): Flow<List<CycleDayEntity>>
}
