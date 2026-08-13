package com.periodflow.core.health_analysis.di

import com.periodflow.core.domain.repository.HealthAnalyzer
import com.periodflow.core.health_analysis.PcosRiskAnalyzer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthAnalysisModule {

    @Binds
    @Singleton
    abstract fun bindHealthAnalyzer(
        pcosRiskAnalyzer: PcosRiskAnalyzer
    ): HealthAnalyzer
}
