package com.periodflow.core.ai.voice

/**
 * Aggregate event stream emitted by [VoiceCompanionOrchestrator].
 *
 * A typical successful lifecycle:
 *   1. `FastGreeting` (< 100 ms) — tagged with emotion, spoken by TTS
 *   2. `FastThinking` — bridge utterance while slow leg computes
 *   3. Many `SlowDelta` events — real answer streamed
 *   4. `SlowDone` — final grounded reply, spoken by TTS with its own emotion
 */
sealed interface VoiceCompanionEvent {
    val emotion: BloomEmotion

    data class FastGreeting(val text: String, override val emotion: BloomEmotion) : VoiceCompanionEvent
    data class FastThinking(val text: String, override val emotion: BloomEmotion) : VoiceCompanionEvent
    data class SlowDelta(val text: String, override val emotion: BloomEmotion = BloomEmotion.NEUTRAL) : VoiceCompanionEvent
    data class SlowDone(val fullText: String, override val emotion: BloomEmotion) : VoiceCompanionEvent
    data class Error(val message: String, override val emotion: BloomEmotion = BloomEmotion.CONCERNED) : VoiceCompanionEvent
}
