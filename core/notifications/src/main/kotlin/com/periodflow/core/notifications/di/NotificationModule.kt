package com.periodflow.core.notifications.di

import com.periodflow.core.domain.repository.NotificationScheduler
import com.periodflow.core.notifications.PeriodNotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationScheduler(
        impl: PeriodNotificationScheduler
    ): NotificationScheduler
}
