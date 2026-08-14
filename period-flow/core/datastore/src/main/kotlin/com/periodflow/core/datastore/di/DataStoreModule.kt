package com.periodflow.core.datastore.di

import com.periodflow.core.datastore.UserPreferencesDataStore
import com.periodflow.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesDataStore,
    ): UserPreferencesRepository
}
