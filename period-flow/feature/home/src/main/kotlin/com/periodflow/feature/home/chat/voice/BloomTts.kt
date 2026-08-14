package com.periodflow.feature.home.chat.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.periodflow.core.ai.voice.BloomEmotion
import java.util.Locale
import java.util.UUID

/**
 * Emotion-aware wrapper around Android's built-in [TextToSpeech].
 *
 * The pitch/rate values live on [BloomEmotion] itself so both fast and slow
 * legs of the voice companion pull from the same source of truth.
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

    /**
     * Speak the given [text] with pitch/rate derived from the [emotion].
     * `isFiller = true` adds a small extra rate bump to keep the greeting punchy.
     */
    fun speakEmotive(text: String, emotion: BloomEmotion, isFiller: Boolean) {
        val engine = tts ?: return
        if (!ready || text.isBlank()) return
        val rateBump = if (isFiller) 0.05f else 0f
        engine.setPitch(emotion.pitch)
        engine.setSpeechRate((emotion.rate + rateBump).coerceIn(0.6f, 1.6f))
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
