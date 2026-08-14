package com.periodflow.core.ai.voice

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import com.periodflow.core.ai.privacy.DataMasker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * On-device fast provider backed by MediaPipe LLM Inference + Gemma-2 2B (int4).
 *
 * Boot behaviour:
 *  - Lazily initialises when [greetingFor]/[thinkingFillerFor] is first called.
 *  - If the model file isn't present (see [GemmaModelManager]) or initialisation
 *    throws, [isReady] stays false and the caller (composite provider) falls
 *    back to the [HeuristicFastProvider] — no ML, zero hallucination risk.
 *
 * Prompts:
 *  - Kept extremely short (< 60 tokens) so first-token latency stays under ~1s
 *    even on mid-range devices.
 *  - User text is masked via [DataMasker] before feeding the local model —
 *    defence in depth in case the model is ever swapped for a networked one.
 */
@Singleton
class MediaPipeGemmaFastProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelManager: GemmaModelManager,
) : FastLlmProvider {

    override val id: String = "mediapipe-gemma-2b-int4"

    @Volatile private var engine: LlmInference? = null
    @Volatile private var initFailed: Boolean = false

    override val isReady: Boolean
        get() = !initFailed && modelManager.isModelPresent()

    private fun ensureEngine(): LlmInference? {
        if (initFailed) return null
        engine?.let { return it }
        if (!modelManager.isModelPresent()) return null
        return synchronized(this) {
            engine ?: runCatching {
                val opts = LlmInferenceOptions.builder()
                    .setModelPath(modelManager.modelFile.absolutePath)
                    .setMaxTokens(96)
                    .setTopK(40)
                    .setTemperature(0.6f)
                    .setRandomSeed(Random.nextInt())
                    .build()
                LlmInference.createFromOptions(context, opts).also { engine = it }
            }.getOrElse {
                initFailed = true
                null
            }
        }
    }

    private fun promptGreeting(masked: String): String = """
        You are Bloom, a warm short-form assistant.
        The user just asked: "$masked"
        Reply with ONE friendly, empathetic acknowledgement (< 15 words).
        Do NOT answer the question. Do NOT invent facts.
    """.trimIndent()

    private fun promptThinking(masked: String): String = """
        You are Bloom. Say ONE short bridge phrase (< 10 words) meaning
        "give me a moment". Vary the wording. Do NOT answer the question.
    """.trimIndent()

    private fun detectEmotion(question: String): BloomEmotion {
        val q = question.lowercase()
        return when {
            listOf("pain", "hurt", "cramp", "ache", "bleeding").any { it in q } -> BloomEmotion.CONCERNED
            listOf("sad", "anxious", "worried", "afraid", "cry").any { it in q } -> BloomEmotion.GENTLE
            listOf("food", "eat", "exercise", "yoga", "tip").any { it in q } -> BloomEmotion.EXCITED
            else -> BloomEmotion.WARM
        }
    }

    override suspend fun greetingFor(userQuestion: String): EmotiveUtterance {
        val emotion = detectEmotion(userQuestion)
        val text = generateOrFallback(promptGreeting(DataMasker.redactUserText(userQuestion)))
            ?: "Sure, let me help."
        return EmotiveUtterance(text, emotion)
    }

    override suspend fun thinkingFillerFor(userQuestion: String): EmotiveUtterance {
        val text = generateOrFallback(promptThinking(DataMasker.redactUserText(userQuestion)))
            ?: "One moment…"
        return EmotiveUtterance(text, BloomEmotion.THINKING)
    }

    private fun generateOrFallback(prompt: String): String? {
        val e = ensureEngine() ?: return null
        return runCatching { e.generateResponse(prompt).trim().takeIf { it.isNotBlank() } }
            .getOrElse {
                initFailed = true
                engine?.close()
                engine = null
                null
            }
    }

    /** Release native resources (e.g. when the user disables voice mode). */
    fun release() {
        synchronized(this) {
            engine?.close()
            engine = null
            initFailed = false
        }
    }
}
