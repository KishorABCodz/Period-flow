package com.periodflow.core.domain.repository

import com.periodflow.core.domain.model.CyclePrediction
import kotlinx.datetime.LocalDate

interface NotificationScheduler {
    suspend fun schedulePeriodicReminders(prediction: CyclePrediction)
    suspend fun cancelAllReminders()
    suspend fun scheduleLateperiodAlert(expectedDate: LocalDate)
}
