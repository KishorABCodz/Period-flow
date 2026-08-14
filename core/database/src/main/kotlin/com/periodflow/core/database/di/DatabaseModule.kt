package com.periodflow.core.database.di

import android.content.Context
import androidx.room.Room
import com.periodflow.core.database.PeriodFlowDatabase
import com.periodflow.core.database.dao.CycleDayDao
import com.periodflow.core.database.dao.CycleDao
import com.periodflow.core.database.dao.AiInsightDao
import com.periodflow.core.database.dao.ChatMessageDao
import com.periodflow.core.database.migration.PeriodFlowMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): PeriodFlowDatabase {
        return Room.databaseBuilder(
            context,
            PeriodFlowDatabase::class.java,
            "periodflow_database",
        )
            .addMigrations(*PeriodFlowMigrations.ALL)
            .build()
    }

    @Provides
    fun provideCycleDayDao(database: PeriodFlowDatabase): CycleDayDao {
        return database.cycleDayDao()
    }

    @Provides
    fun provideCycleDao(database: PeriodFlowDatabase): CycleDao {
        return database.cycleDao()
    }

    @Provides
    fun provideAiInsightDao(database: PeriodFlowDatabase): AiInsightDao {
        return database.aiInsightDao()
    }

    @Provides
    fun provideChatMessageDao(database: PeriodFlowDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }
}
