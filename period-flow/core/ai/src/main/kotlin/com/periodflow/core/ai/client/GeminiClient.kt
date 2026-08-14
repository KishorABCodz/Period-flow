package com.periodflow.core.ai.client

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.periodflow.core.ai.BuildConfig

/**
 * Thin factory around Google's official Generative AI Kotlin SDK.
 * Kept internal to the module — repositories are the public surface.
 *
 * Model: `gemini-2.5-flash` (mapped from user's request "Gemini 3 Flash";
 * upgrade to `gemini-3-flash-preview` when the SDK/backend ships it).
 */
internal object GeminiClient {

    private const val MODEL_NAME = "gemini-2.5-flash"

    fun create(): GenerativeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.6f
            topK = 32
            topP = 0.95f
            maxOutputTokens = 512
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
        ),
    )

    val isConfigured: Boolean get() = BuildConfig.GEMINI_API_KEY.isNotBlank()
}
