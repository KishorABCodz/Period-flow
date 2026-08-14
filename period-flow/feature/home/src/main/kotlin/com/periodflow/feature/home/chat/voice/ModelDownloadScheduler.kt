package com.periodflow.feature.home.chat.voice

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * Convenience wrapper around [WorkManager] for the deferred Wi-Fi download.
 *
 * The unique work name means enqueuing twice is safe — the second call
 * REPLACES the first, so users can toggle the option without piling up jobs.
 */
object ModelDownloadScheduler {

    fun enqueue(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        val request = OneTimeWorkRequestBuilder<WifiModelDownloadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WifiModelDownloadWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WifiModelDownloadWorker.UNIQUE_NAME)
    }

    /** Observe whether a deferred download is currently queued/running. */
    fun observePending(context: Context): Flow<Boolean> {
        val wm = WorkManager.getInstance(context)
        return wm.getWorkInfosForUniqueWorkFlow(WifiModelDownloadWorker.UNIQUE_NAME)
            .map { infos ->
                infos.any {
                    it.state == WorkInfo.State.ENQUEUED ||
                        it.state == WorkInfo.State.RUNNING ||
                        it.state == WorkInfo.State.BLOCKED
                }
            }
    }
}
