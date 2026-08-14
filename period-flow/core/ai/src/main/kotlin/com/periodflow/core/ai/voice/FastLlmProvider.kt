package com.periodflow.core.ai.voice

/**
 * Producer of near-instant "filler" utterances for the voice companion.
 *
 * The fast provider runs first (while the slow model is still generating) so
 * the user hears a natural greeting or thinking phrase within ~100ms.
 *
 * Implementations MUST NOT hallucinate factual claims — they exist purely to
 * bridge the latency gap. Ship real facts only via the slow (grounded) model.
 *
 * ## Plugging in an on-device OSS model
 *
 * The default binding is [HeuristicFastProvider] (rule-based, no ML, zero risk).
 * A [MediaPipeGemmaFastProvider] is also available: it activates automatically
 * once the user consents to a one-time model download.
 *
 * Alternate future path: MLC-LLM + Qwen 2.5 0.5B/1.5B. Implement this interface,
 * swap the binding in [com.periodflow.core.ai.di.AiBindingsModule].
 *
 * All implementations should still funnel user text through
 * [com.periodflow.core.ai.privacy.DataMasker] before sending it off-device.
 */
interface FastLlmProvider {

    /** Human-readable id for debugging/logging. */
    val id: String

    /** True when the underlying engine is ready to serve requests. */
    val isReady: Boolean

    /** Returns a short (< 20 word) filler utterance, tagged with emotion. */
    suspend fun greetingFor(userQuestion: String): EmotiveUtterance

    /** Returns a short (< 15 word) "thinking" bridge utterance, tagged with emotion. */
    suspend fun thinkingFillerFor(userQuestion: String): EmotiveUtterance
}
