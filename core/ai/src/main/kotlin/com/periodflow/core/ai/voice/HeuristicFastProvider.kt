package com.periodflow.core.ai.voice

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Zero-ML, zero-hallucination filler generator.
 *
 * Deterministic phrases keyed off cheap intent detection (question type + keywords),
 * tagged with the emotion the UI should express while speaking them.
 *
 * Never states facts. Never claims certainty. Just acknowledges + buys time.
 */
@Singleton
class HeuristicFastProvider @Inject constructor() : FastLlmProvider {

    override val id: String = "heuristic-v2"
    override val isReady: Boolean = true

    private val neutralGreetings = listOf(
        "Hmm, good one.",
        "Okay — let me look at that.",
        "Sure, I can help.",
        "Right, let's see.",
        "Got it.",
    )

    private val warmGreetings = listOf(
        "Aw, I hear you.",
        "That makes sense to ask.",
        "Totally fair question.",
    )

    private val gentleGreetings = listOf(
        "It's okay to ask about this.",
        "You're not alone in wondering.",
        "Deep breath — I've got you.",
    )

    private val excitedGreetings = listOf(
        "Oh, love this question!",
        "Yes — glad you asked.",
        "Ooh, fun one.",
    )

    private val thinkingBridges = listOf(
        "Give me one moment…",
        "Just checking your cycle context…",
        "Thinking this through…",
    )

    private val excitedBridges = listOf(
        "Oh — I actually have something on this.",
        "Right, that's a common one.",
        "Okay, here's what I know…",
    )

    private val gentleBridges = listOf(
        "Let me think carefully…",
        "Taking a moment on this one…",
    )

    private val painKeywords = setOf(
        "pain", "hurt", "cramp", "ache", "sore", "bleeding", "heavy",
    )
    private val distressKeywords = setOf(
        "sad", "anxious", "worried", "scared", "afraid", "stressed", "cry",
    )
    private val excitedKeywords = setOf(
        "food", "eat", "diet", "exercise", "workout", "yoga", "vitamin",
        "supplement", "recipe", "tip",
    )
    private val neutralGreetingKeywords = setOf("hi", "hello", "hey", "yo")

    private fun detectEmotion(question: String): BloomEmotion {
        val q = question.lowercase()
        return when {
            painKeywords.any { it in q } -> BloomEmotion.CONCERNED
            distressKeywords.any { it in q } -> BloomEmotion.GENTLE
            excitedKeywords.any { it in q } -> BloomEmotion.EXCITED
            neutralGreetingKeywords.any { q.startsWith(it) } -> BloomEmotion.WARM
            else -> BloomEmotion.NEUTRAL
        }
    }

    override suspend fun greetingFor(userQuestion: String): EmotiveUtterance {
        val emotion = detectEmotion(userQuestion)
        val pool = when (emotion) {
            BloomEmotion.CONCERNED -> gentleGreetings
            BloomEmotion.GENTLE -> gentleGreetings
            BloomEmotion.WARM -> warmGreetings
            BloomEmotion.EXCITED -> excitedGreetings
            else -> neutralGreetings
        }
        return EmotiveUtterance(pool[Random.nextInt(pool.size)], emotion)
    }

    override suspend fun thinkingFillerFor(userQuestion: String): EmotiveUtterance {
        val emotion = detectEmotion(userQuestion)
        val pool = when (emotion) {
            BloomEmotion.EXCITED -> excitedBridges
            BloomEmotion.CONCERNED, BloomEmotion.GENTLE -> gentleBridges
            else -> thinkingBridges
        }
        // Bridge phrases are always framed as "THINKING" (the movement, not the topic).
        return EmotiveUtterance(pool[Random.nextInt(pool.size)], BloomEmotion.THINKING)
    }
}
