package com.periodflow.core.ai.voice

/**
 * Emotional expression tag attached to every voice-companion utterance.
 *
 * The fast provider infers an emotion from keywords, the TTS layer maps it to
 * pitch/rate variations, and the chat UI displays it as a small animated
 * indicator so users see (and hear) Bloom react to what they're going through.
 */
enum class BloomEmotion(
    /** Multiplier for TTS pitch (1.0 = neutral). */
    val pitch: Float,
    /** Multiplier for TTS rate (1.0 = neutral). */
    val rate: Float,
    /** Compact emoji label the UI can show as a fallback if no icon is drawn. */
    val glyph: String,
) {
    NEUTRAL(pitch = 1.0f, rate = 1.0f, glyph = "🙂"),
    WARM(pitch = 1.05f, rate = 0.95f, glyph = "🤗"),
    THINKING(pitch = 0.98f, rate = 1.05f, glyph = "🤔"),
    EXCITED(pitch = 1.15f, rate = 1.15f, glyph = "✨"),
    GENTLE(pitch = 0.95f, rate = 0.9f, glyph = "🌸"),
    CONCERNED(pitch = 0.92f, rate = 0.9f, glyph = "💛"),
}
