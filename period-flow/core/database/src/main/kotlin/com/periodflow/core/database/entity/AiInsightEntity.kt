package com.periodflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached last AI-generated narrative.
 * Uses a fixed row id (1) so it's effectively a singleton — upsert-friendly.
 *
 * `basedOnRiskScore` lets consumers detect that the underlying deterministic
 * report has changed and invalidate the cache automatically.
 */
@Entity(tableName = "ai_insight")
data class AiInsightEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val narrative: String,
    val updatedAtEpochMilli: Long,
    val basedOnRiskScore: Int = -1,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
