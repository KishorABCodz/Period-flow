package com.periodflow.core.domain.repository

import com.periodflow.core.domain.model.HealthAnalysisReport
import com.periodflow.core.domain.model.HealthIndicator

interface HealthAnalyzer {
    suspend fun analyzeHistory(): HealthAnalysisReport?
    fun getIndicatorDetails(indicator: HealthIndicator): String
}
