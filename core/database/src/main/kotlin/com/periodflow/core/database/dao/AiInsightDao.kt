package com.periodflow.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.periodflow.core.database.entity.AiInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiInsightDao {

    @Query("SELECT * FROM ai_insight WHERE id = :id LIMIT 1")
    fun observe(id: Int = AiInsightEntity.SINGLETON_ID): Flow<AiInsightEntity?>

    @Query("SELECT * FROM ai_insight WHERE id = :id LIMIT 1")
    suspend fun getOnce(id: Int = AiInsightEntity.SINGLETON_ID): AiInsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AiInsightEntity)

    @Query("DELETE FROM ai_insight")
    suspend fun clear()
}
