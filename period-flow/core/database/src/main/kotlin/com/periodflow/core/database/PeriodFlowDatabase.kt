package com.periodflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.periodflow.core.database.dao.AiInsightDao
import com.periodflow.core.database.dao.ChatMessageDao
import com.periodflow.core.database.dao.CycleDayDao
import com.periodflow.core.database.dao.CycleDao
import com.periodflow.core.database.entity.AiInsightEntity
import com.periodflow.core.database.entity.ChatMessageEntity
import com.periodflow.core.database.entity.CycleDayEntity
import com.periodflow.core.database.entity.CycleEntity

@Database(
    entities = [
        CycleDayEntity::class,
        CycleEntity::class,
        AiInsightEntity::class,
        ChatMessageEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class PeriodFlowDatabase : RoomDatabase() {
    abstract fun cycleDayDao(): CycleDayDao
    abstract fun cycleDao(): CycleDao
    abstract fun aiInsightDao(): AiInsightDao
    abstract fun chatMessageDao(): ChatMessageDao
}
