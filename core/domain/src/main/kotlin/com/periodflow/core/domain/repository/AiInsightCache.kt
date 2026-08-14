package com.periodflow.core.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Cached AI-generated health-insight narrative.
 * Backed by Room (single-row store) so users see the latest text instantly
 * on repeat visits, even before a fresh stream completes.
 */
interface AiInsightCache {
    /** Latest cached narrative + epoch-millis timestamp, or `null` if none yet. */
    fun observe(): Flow<CachedAiInsight?>

    suspend fun getOnce(): CachedAiInsight?

    /**
     * @param basedOnRiskScore the deterministic risk score the narrative was generated from.
     *                         Consumers should invalidate the cache when the score changes.
     */
    suspend fun save(narrative: String, basedOnRiskScore: Int)

    suspend fun clear()
}

data class CachedAiInsight(
    val narrative: String,
    val updatedAtEpochMilli: Long,
    val basedOnRiskScore: Int,
)
