package com.periodflow.core.ai.voice

/**
 * A short utterance tagged with its intended emotional colour.
 * Used by [FastLlmProvider] so downstream TTS + UI can render Bloom's mood.
 */
data class EmotiveUtterance(
    val text: String,
    val emotion: BloomEmotion,
)
