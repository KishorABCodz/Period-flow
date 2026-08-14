package com.periodflow.feature.home.chat.voice

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.periodflow.core.ai.voice.GemmaModelManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Deferred Gemma model download that only runs when the device is on an
 * unmetered network. Scheduled by [ModelDownloadScheduler] when the user
 * picks "Wait for Wi-Fi" in the metered-warning dialog.
 *
 * On success: leaves the downloaded file in place — [GemmaModelManager]
 * picks it up automatically the next time Bloom needs the fast provider.
 */
@HiltWorker
class WifiModelDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val modelManager: GemmaModelManager,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (modelManager.isModelPresent()) return Result.success()
        val downloader = ModelDownloader(applicationContext, modelManager)
        return runCatching {
            val last = downloader.download().first {
                it is ModelDownloader.DownloadProgress.Success ||
                    it is ModelDownloader.DownloadProgress.Failed
            }
            when (last) {
                is ModelDownloader.DownloadProgress.Success -> Result.success()
                is ModelDownloader.DownloadProgress.Failed -> Result.retry()
                else -> Result.retry()
            }
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val UNIQUE_NAME = "wifi_gemma_download"
    }
}
