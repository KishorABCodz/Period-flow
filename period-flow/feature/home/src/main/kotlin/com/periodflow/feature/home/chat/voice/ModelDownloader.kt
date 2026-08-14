package com.periodflow.feature.home.chat.voice

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import com.periodflow.core.ai.voice.GemmaModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

/**
 * Consented download of the Gemma-2 2B model file using Android's built-in
 * [DownloadManager] — no third-party library.
 *
 * Emits [DownloadProgress] events until either the file has been downloaded
 * and validated by [GemmaModelManager], or the download fails.
 */
class ModelDownloader(
    private val context: Context,
    private val modelManager: GemmaModelManager,
) {

    sealed interface DownloadProgress {
        data class Running(val downloadedBytes: Long, val totalBytes: Long) : DownloadProgress {
            val ratio: Float get() =
                if (totalBytes <= 0) 0f else (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
        }
        data object Success : DownloadProgress
        data class Failed(val reason: String) : DownloadProgress
    }

    /**
     * Kicks off the download and returns a Flow of progress events.
     * If [urlOverride] is null the URL configured in `local.properties` is used.
     */
    fun download(urlOverride: String? = null): Flow<DownloadProgress> = flow {
        val url = (urlOverride ?: modelManager.configuredUrl)?.takeIf { it.isNotBlank() }
        if (url == null) {
            emit(DownloadProgress.Failed("No Gemma model URL configured (add GEMMA_MODEL_URL to local.properties)."))
            return@flow
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val destFile = modelManager.modelFile
        if (destFile.exists()) destFile.delete()

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("PeriodFlow · Voice model")
            setDescription("Downloading Bloom's on-device fast model")
            setDestinationUri(Uri.fromFile(destFile))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setAllowedOverMetered(false)
        }
        val id = dm.enqueue(request)

        while (true) {
            val cursor: Cursor? = dm.query(DownloadManager.Query().setFilterById(id))
            cursor?.use { c ->
                if (!c.moveToFirst()) {
                    emit(DownloadProgress.Failed("Download vanished."))
                    return@flow
                }
                val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        if (modelManager.isModelPresent()) emit(DownloadProgress.Success)
                        else emit(DownloadProgress.Failed("Downloaded file is too small or missing."))
                        return@flow
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                        emit(DownloadProgress.Failed("Download failed (reason $reason)."))
                        return@flow
                    }
                    else -> emit(DownloadProgress.Running(downloaded, total))
                }
            } ?: run {
                emit(DownloadProgress.Failed("Cursor was null."))
                return@flow
            }
            delay(500)
        }
    }.flowOn(Dispatchers.IO)

    /** Delete a previously-downloaded model (e.g. when the user turns voice mode off). */
    fun purgeModel(): Boolean {
        val f: File = modelManager.modelFile
        return if (f.exists()) f.delete() else true
    }

    /** True when the active network is billed / metered (cellular, hotspot, etc.). */
    fun isNetworkMetered(): Boolean = try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.isActiveNetworkMetered
    } catch (_: Exception) {
        false
    }
}
