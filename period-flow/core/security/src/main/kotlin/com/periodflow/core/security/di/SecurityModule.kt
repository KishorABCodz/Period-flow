package com.periodflow.core.security.di

import com.periodflow.core.domain.repository.AppAuthenticator
import com.periodflow.core.security.BiometricAuthenticator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindAppAuthenticator(
        biometricAuthenticator: BiometricAuthenticator
    ): AppAuthenticator
}
