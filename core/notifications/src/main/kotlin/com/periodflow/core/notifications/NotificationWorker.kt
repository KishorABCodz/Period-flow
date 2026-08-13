package com.periodflow.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val title = inputData.getString(KEY_TITLE) ?: return@withContext Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return@withContext Result.failure()
        
        // Context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager would be used here
        
        Result.success()
    }

    companion object {
        const val KEY_TITLE = "notification_title"
        const val KEY_MESSAGE = "notification_message"
    }
}
