package com.periodflow.core.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.periodflow.core.domain.model.CyclePrediction
import com.periodflow.core.domain.repository.NotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PeriodNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun schedulePeriodicReminders(prediction: CyclePrediction) {
        val data = Data.Builder()
            .putString(NotificationWorker.KEY_TITLE, "Period Reminder")
            .putString(NotificationWorker.KEY_MESSAGE, "Your period is expected soon.")
            .build()

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME_PERIOD_REMINDER,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override suspend fun cancelAllReminders() {
        workManager.cancelAllWork()
    }

    override suspend fun scheduleLateperiodAlert(expectedDate: LocalDate) {
        val data = Data.Builder()
            .putString(NotificationWorker.KEY_TITLE, "Late Period Alert")
            .putString(NotificationWorker.KEY_MESSAGE, "Your period seems to be late.")
            .build()

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .setInitialDelay(2, TimeUnit.DAYS)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME_LATE_ALERT,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        private const val WORK_NAME_PERIOD_REMINDER = "period_reminder_work"
        private const val WORK_NAME_LATE_ALERT = "late_alert_work"
    }
}
