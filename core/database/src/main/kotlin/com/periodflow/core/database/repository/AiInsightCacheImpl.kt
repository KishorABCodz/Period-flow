package com.periodflow.core.database.repository

import com.periodflow.core.database.dao.AiInsightDao
import com.periodflow.core.database.entity.AiInsightEntity
import com.periodflow.core.domain.repository.AiInsightCache
import com.periodflow.core.domain.repository.CachedAiInsight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AiInsightCacheImpl @Inject constructor(
    private val dao: AiInsightDao,
) : AiInsightCache {

    override fun observe(): Flow<CachedAiInsight?> =
        dao.observe().map { it?.toDomain() }

    override suspend fun getOnce(): CachedAiInsight? =
        dao.getOnce()?.toDomain()

    override suspend fun save(narrative: String, basedOnRiskScore: Int) {
        dao.upsert(
            AiInsightEntity(
                narrative = narrative,
                updatedAtEpochMilli = System.currentTimeMillis(),
                basedOnRiskScore = basedOnRiskScore,
            )
        )
    }

    override suspend fun clear() {
        dao.clear()
    }

    private fun AiInsightEntity.toDomain() = CachedAiInsight(
        narrative = narrative,
        updatedAtEpochMilli = updatedAtEpochMilli,
        basedOnRiskScore = basedOnRiskScore,
    )
}
