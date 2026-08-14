package com.periodflow.core.ai.voice

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Delegates to [MediaPipeGemmaFastProvider] when the on-device model is present
 * and initialised; otherwise falls back to the deterministic [HeuristicFastProvider].
 *
 * This means the voice companion works out of the box (rule-based) and silently
 * upgrades to Gemma-2 2B once the user consents to the one-time model download.
 */
@Singleton
class CompositeFastProvider @Inject constructor(
    private val gemma: MediaPipeGemmaFastProvider,
    private val heuristic: HeuristicFastProvider,
) : FastLlmProvider {

    override val id: String
        get() = if (gemma.isReady) gemma.id else heuristic.id

    override val isReady: Boolean = true

    override suspend fun greetingFor(userQuestion: String): EmotiveUtterance =
        if (gemma.isReady) gemma.greetingFor(userQuestion)
        else heuristic.greetingFor(userQuestion)

    override suspend fun thinkingFillerFor(userQuestion: String): EmotiveUtterance =
        if (gemma.isReady) gemma.thinkingFillerFor(userQuestion)
        else heuristic.thinkingFillerFor(userQuestion)
}
