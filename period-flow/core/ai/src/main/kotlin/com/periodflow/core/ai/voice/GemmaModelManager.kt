package com.periodflow.core.ai.voice

import android.content.Context
import com.periodflow.core.ai.BuildConfig
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the on-device Gemma model file location and tells the app whether
 * a model has been downloaded yet.
 *
 * Zero-hallucination protocol:
 * - The model URL is NEVER hard-coded. It comes from `local.properties`
 *   (`GEMMA_MODEL_URL=...`) or from a user-provided string at runtime.
 * - `isModelPresent()` is the sole source of truth for enabling the on-device
 *   fast provider.
 */
@Singleton
class GemmaModelManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {

    val modelFile: File get() = File(context.filesDir, MODEL_FILE_NAME)

    fun isModelPresent(): Boolean = modelFile.exists() && modelFile.length() > MIN_VALID_SIZE_BYTES

    /**
     * URL configured via `local.properties → GEMMA_MODEL_URL` (surfaced through
     * BuildConfig). Returns null / empty when the developer has not set one — the
     * UI can then let the user paste a URL manually.
     */
    val configuredUrl: String? get() = BuildConfig.GEMMA_MODEL_URL.takeIf { it.isNotBlank() }

    fun deleteModel() {
        if (modelFile.exists()) modelFile.delete()
    }

    companion object {
        const val MODEL_FILE_NAME = "gemma-2b-it-cpu-int4.bin"
        /** ~64 MB — sanity check to make sure the file actually finished downloading. */
        const val MIN_VALID_SIZE_BYTES: Long = 64L * 1024 * 1024
    }
}
