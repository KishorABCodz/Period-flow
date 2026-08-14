package com.periodflow.feature.home.chat.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/**
 * Minimal wrapper around Android's built-in [TextToSpeech].
 *
 * Two configurable "voices":
 *   - [speakFiller] uses a slightly faster, warmer rate for fast-filler lines.
 *   - [speakAnswer] uses a calm, default cadence for the grounded answer.
 */
class BloomTts(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready: Boolean = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                ready = true
            }
        }
    }

    fun speakFiller(text: String) = enqueue(text, rate = 1.1f, pitch = 1.05f)

    fun speakAnswer(text: String) = enqueue(text, rate = 1.0f, pitch = 1.0f)

    private fun enqueue(text: String, rate: Float, pitch: Float) {
        val engine = tts ?: return
        if (!ready || text.isBlank()) return
        engine.setSpeechRate(rate)
        engine.setPitch(pitch)
        engine.speak(text, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    /** Optional: subscribe to per-utterance done/error events. */
    fun setListener(
        onStart: (String) -> Unit = {},
        onDone: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) = onStart(utteranceId)
            override fun onDone(utteranceId: String) = onDone(utteranceId)
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) = onError(utteranceId)
        })
    }
}
