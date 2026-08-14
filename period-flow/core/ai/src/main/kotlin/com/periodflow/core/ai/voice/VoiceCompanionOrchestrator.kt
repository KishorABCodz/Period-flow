package com.periodflow.core.ai.voice

import com.periodflow.core.ai.model.AiStreamEvent
import com.periodflow.core.ai.privacy.DataMasker
import com.periodflow.core.ai.repository.ChatTurn
import com.periodflow.core.ai.repository.GeminiAiRepository
import com.periodflow.core.domain.model.CyclePhase
import com.periodflow.core.domain.model.RecentLogSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dual-model voice companion orchestrator.
 *
 * Pipeline:
 *   fast provider  ─►  emits `FastGreeting` + `FastThinking` immediately
 *                      (spoken by TTS in the UI layer)
 *   slow provider  ─►  streams `SlowDelta` from Gemini in parallel
 *                      (rendered into the chat bubble word-by-word)
 *                      then `SlowDone(fullText)`
 *
 * Privacy: the user's question is masked through [DataMasker] before it ever
 * reaches the slow (network-backed) model. The fast provider stays on-device.
 */
@Singleton
class VoiceCompanionOrchestrator @Inject constructor(
    private val fastProvider: FastLlmProvider,
    private val geminiAiRepository: GeminiAiRepository,
) {

    fun ask(
        userQuestion: String,
        history: List<ChatTurn>,
        cyclePhase: CyclePhase?,
        cycleDayNumber: Int,
        recent: RecentLogSummary?,
    ): Flow<VoiceCompanionEvent> = channelFlow {
        val maskedQuestion = DataMasker.redactUserText(userQuestion)
        // The greeting's emotion becomes the "answer" emotion too — the fast provider
        // has the earliest read of the user's tone, so we reuse it for consistency.
        var inferredEmotion: BloomEmotion = BloomEmotion.NEUTRAL

        // 1. Fast filler leg — runs on-device.
        launch {
            runCatching {
                val greeting = fastProvider.greetingFor(userQuestion)
                inferredEmotion = greeting.emotion
                send(VoiceCompanionEvent.FastGreeting(greeting.text, greeting.emotion))
                val bridge = fastProvider.thinkingFillerFor(userQuestion)
                send(VoiceCompanionEvent.FastThinking(bridge.text, bridge.emotion))
            }
        }

        // 2. Slow, grounded leg — streams Gemini reply.
        launch {
            val buffer = StringBuilder()
            val historyWithQuestion = history + ChatTurn(isUser = true, text = maskedQuestion)
            geminiAiRepository.streamChatReply(
                history = historyWithQuestion,
                cyclePhase = cyclePhase,
                cycleDayNumber = cycleDayNumber,
                recent = recent,
            ).collect { event ->
                when (event) {
                    is AiStreamEvent.Delta -> {
                        buffer.append(event.text)
                        send(VoiceCompanionEvent.SlowDelta(event.text, inferredEmotion))
                    }
                    AiStreamEvent.Done -> {
                        val final = buffer.toString().trim().ifBlank {
                            "I'm not sure how to help with that."
                        }
                        send(VoiceCompanionEvent.SlowDone(final, inferredEmotion))
                    }
                    is AiStreamEvent.Error -> {
                        send(VoiceCompanionEvent.Error(event.message))
                    }
                }
            }
        }

        awaitClose { /* channelFlow cleans up child coroutines automatically */ }
    }.flowOn(Dispatchers.IO)
}
