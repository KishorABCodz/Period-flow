package com.periodflow.core.database.di

import com.periodflow.core.database.SeedDataManagerImpl
import com.periodflow.core.database.repository.CycleDayRepositoryImpl
import com.periodflow.core.database.repository.CycleRepositoryImpl
import com.periodflow.core.domain.repository.CycleDayRepository
import com.periodflow.core.domain.repository.CycleRepository
import com.periodflow.core.domain.repository.DataSeeder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCycleDayRepository(
        impl: CycleDayRepositoryImpl,
    ): CycleDayRepository

    @Binds
    @Singleton
    abstract fun bindCycleRepository(
        impl: CycleRepositoryImpl,
    ): CycleRepository

    @Binds
    @Singleton
    abstract fun bindDataSeeder(
        impl: SeedDataManagerImpl,
    ): DataSeeder
}
