package com.periodflow.feature.home.chat.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat

/**
 * Thin wrapper around Android's platform [SpeechRecognizer] so the Compose
 * layer can start/stop dictation without touching Android's callback API
 * directly. Emits partial and final transcripts + errors through simple lambdas.
 *
 * Requires `RECORD_AUDIO` permission (declared in the app manifest) and a
 * user-granted runtime permission. When unavailable (e.g. no Google speech
 * service installed) [isAvailable] returns false.
 */
class VoiceInputController(
    private val context: Context,
    private val onPartial: (String) -> Unit,
    private val onFinal: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onStateChanged: (Boolean) -> Unit,
) {

    private var recognizer: SpeechRecognizer? = null
    private var listening: Boolean = false

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED

    fun start() {
        if (listening) return
        if (!isAvailable) {
            onError("Voice input isn't available on this device.")
            return
        }
        if (!hasPermission()) {
            onError("Microphone permission is required.")
            return
        }

        val engine = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            recognizer = it
            it.setRecognitionListener(listener)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        listening = true
        onStateChanged(true)
        engine.startListening(intent)
    }

    fun stop() {
        if (!listening) return
        recognizer?.stopListening()
    }

    fun release() {
        recognizer?.destroy()
        recognizer = null
        listening = false
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            listening = false
            onStateChanged(false)
        }

        override fun onError(error: Int) {
            listening = false
            onStateChanged(false)
            val msg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied."
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error — please check your connection."
                SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Try again?"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy — please wait."
                SpeechRecognizer.ERROR_SERVER -> "Server error."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything. Try again?"
                else -> "Voice input error ($error)."
            }
            onError(msg)
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            listening = false
            onStateChanged(false)
            if (text.isNotBlank()) onFinal(text)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (text.isNotBlank()) onPartial(text)
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}
