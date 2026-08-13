package com.periodflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.periodflow.core.database.dao.CycleDayDao
import com.periodflow.core.database.dao.CycleDao
import com.periodflow.core.database.entity.CycleDayEntity
import com.periodflow.core.database.entity.CycleEntity

@Database(
    entities = [
        CycleDayEntity::class,
        CycleEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class PeriodFlowDatabase : RoomDatabase() {
    abstract fun cycleDayDao(): CycleDayDao
    abstract fun cycleDao(): CycleDao
}
