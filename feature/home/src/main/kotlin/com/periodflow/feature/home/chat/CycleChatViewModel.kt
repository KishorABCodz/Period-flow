package com.periodflow.feature.home.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodflow.core.ai.model.AiStreamEvent
import com.periodflow.core.ai.repository.ChatTurn
import com.periodflow.core.ai.repository.GeminiAiRepository
import com.periodflow.core.ai.voice.VoiceCompanionEvent
import com.periodflow.core.ai.voice.VoiceCompanionOrchestrator
import com.periodflow.core.domain.model.CyclePhase
import com.periodflow.core.domain.repository.ChatHistoryRepository
import com.periodflow.core.domain.usecase.GetCurrentPhaseUseCase
import com.periodflow.core.domain.usecase.GetRecentLogSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false,
)

data class CycleChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class CycleChatViewModel @Inject constructor(
    private val geminiAiRepository: GeminiAiRepository,
    private val getCurrentPhaseUseCase: GetCurrentPhaseUseCase,
    private val getRecentLogSummaryUseCase: GetRecentLogSummaryUseCase,
    private val chatHistoryRepository: ChatHistoryRepository,
    private val voiceCompanionOrchestrator: VoiceCompanionOrchestrator,
    private val userPreferencesRepository: com.periodflow.core.domain.repository.UserPreferencesRepository,
    private val gemmaModelManager: com.periodflow.core.ai.voice.GemmaModelManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CycleChatUiState())
    val uiState: StateFlow<CycleChatUiState> = _uiState.asStateFlow()

    /** One-shot events the UI observes for TTS playback in voice mode. */
    private val _voiceEvents = MutableSharedFlow<VoiceCompanionEvent>(extraBufferCapacity = 16)
    val voiceEvents: SharedFlow<VoiceCompanionEvent> = _voiceEvents.asSharedFlow()

    /** Emitted as Bloom's current mood; the UI can bind an animated indicator to it. */
    private val _currentEmotion = MutableStateFlow(com.periodflow.core.ai.voice.BloomEmotion.NEUTRAL)
    val currentEmotion: StateFlow<com.periodflow.core.ai.voice.BloomEmotion> = _currentEmotion.asStateFlow()

    /** Persisted voice-mode preference (DataStore-backed). */
    val voiceModeEnabled: StateFlow<Boolean> = userPreferencesRepository.userPreferences
        .map { it.isVoiceModeEnabled }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, false)

    fun setVoiceModeEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setVoiceModeEnabled(enabled) }
    }

    // ---------- Gemma model download flow ----------

    private val downloader by lazy {
        com.periodflow.feature.home.chat.voice.ModelDownloader(appContext, gemmaModelManager)
    }

    private val _hasGemmaModel = MutableStateFlow(gemmaModelManager.isModelPresent())
    val hasGemmaModel: StateFlow<Boolean> = _hasGemmaModel.asStateFlow()

    private val _downloadProgress = MutableStateFlow<com.periodflow.feature.home.chat.voice.ModelDownloader.DownloadProgress?>(null)
    val downloadProgress: StateFlow<com.periodflow.feature.home.chat.voice.ModelDownloader.DownloadProgress?> = _downloadProgress.asStateFlow()

    /** True when we've deferred a download because the user is on metered data. */
    private val _pendingMeteredConfirm = MutableStateFlow(false)
    val pendingMeteredConfirm: StateFlow<Boolean> = _pendingMeteredConfirm.asStateFlow()

    /**
     * Request a download. If the active network is metered, sets
     * [pendingMeteredConfirm] instead and waits for the UI to confirm.
     */
    fun requestModelDownload() {
        if (downloader.isNetworkMetered()) {
            _pendingMeteredConfirm.value = true
        } else {
            startModelDownload()
        }
    }

    /** Called by the confirmation dialog when the user accepts metered download. */
    fun confirmMeteredDownload() {
        _pendingMeteredConfirm.value = false
        startModelDownload()
    }

    fun cancelMeteredDownload() {
        _pendingMeteredConfirm.value = false
    }

    fun startModelDownload(urlOverride: String? = null) {
        viewModelScope.launch {
            downloader.download(urlOverride).collect { p ->
                _downloadProgress.value = p
                if (p is com.periodflow.feature.home.chat.voice.ModelDownloader.DownloadProgress.Success) {
                    _hasGemmaModel.value = true
                }
            }
        }
    }

    fun dismissDownloadState() {
        _downloadProgress.value = null
    }

    fun purgeGemmaModel() {
        viewModelScope.launch {
            downloader.purgeModel()
            _hasGemmaModel.value = false
        }
    }

    private var streamJob: Job? = null
    // Local, non-persisted ids for the currently streaming placeholder.
    private var streamingLocalId: Long = -1L

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = chatHistoryRepository.getMessages()
            val restored = history.map {
                ChatMessage(id = it.id, isUser = it.isUser, text = it.text, isStreaming = false)
            }
            val greeting = if (restored.isEmpty()) {
                listOf(
                    ChatMessage(
                        id = 0L,
                        isUser = false,
                        text = "Hi, I'm Bloom. Ask me anything about your cycle — symptoms, mood, or self-care ideas.",
                    )
                )
            } else emptyList()

            _uiState.value = _uiState.value.copy(messages = greeting + restored)
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _uiState.value.isSending) return

        viewModelScope.launch {
            // Persist the (unmasked) user message locally — masking only happens on egress to the LLM.
            val userDbId = chatHistoryRepository.addMessage(isUser = true, text = trimmed)
            val userMsg = ChatMessage(id = userDbId, isUser = true, text = trimmed)

            // Local placeholder for the streaming assistant reply (not persisted until Done).
            streamingLocalId = -System.currentTimeMillis()
            val placeholder = ChatMessage(
                id = streamingLocalId, isUser = false, text = "", isStreaming = true,
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMsg + placeholder,
                isSending = true,
                errorMessage = null,
            )

            val (phase, dayInCycle) = runCatching { getCurrentPhaseUseCase() }
                .getOrDefault(CyclePhase.UNKNOWN to 0)

            // Aggregated, anonymised context for the LLM.
            val recent = runCatching { getRecentLogSummaryUseCase() }.getOrNull()

            val history = _uiState.value.messages
                .filter { !it.isStreaming }
                .map { ChatTurn(isUser = it.isUser, text = it.text) }

            streamJob?.cancel()
            streamJob = launch {
                val buffer = StringBuilder()
                geminiAiRepository.streamChatReply(
                    history = history,
                    cyclePhase = phase,
                    cycleDayNumber = dayInCycle,
                    recent = recent,
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Delta -> {
                            buffer.append(event.text)
                            updatePlaceholder(streamingLocalId, buffer.toString(), streaming = true)
                        }
                        AiStreamEvent.Done -> {
                            val final = buffer.toString().trim()
                                .ifBlank { "I'm not sure how to help with that." }
                            // Persist the assistant reply.
                            val assistantDbId = chatHistoryRepository.addMessage(
                                isUser = false, text = final,
                            )
                            // Replace the temporary placeholder id with the DB id.
                            _uiState.value = _uiState.value.copy(
                                messages = _uiState.value.messages.map {
                                    if (it.id == streamingLocalId) {
                                        it.copy(id = assistantDbId, text = final, isStreaming = false)
                                    } else it
                                },
                                isSending = false,
                            )
                        }
                        is AiStreamEvent.Error -> {
                            removePlaceholder(streamingLocalId)
                            _uiState.value = _uiState.value.copy(
                                isSending = false,
                                errorMessage = event.message,
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatHistoryRepository.clear()
            loadHistory()
        }
    }

    /**
     * Voice-companion path: runs the fast filler + slow (grounded) leg in parallel,
     * emits [VoiceCompanionEvent]s for TTS, and persists the final assistant reply
     * exactly like [sendMessage].
     */
    fun sendVoiceMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _uiState.value.isSending) return

        viewModelScope.launch {
            val userDbId = chatHistoryRepository.addMessage(isUser = true, text = trimmed)
            val userMsg = ChatMessage(id = userDbId, isUser = true, text = trimmed)

            streamingLocalId = -System.currentTimeMillis()
            val placeholder = ChatMessage(
                id = streamingLocalId, isUser = false, text = "", isStreaming = true,
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMsg + placeholder,
                isSending = true,
                errorMessage = null,
            )

            val (phase, dayInCycle) = runCatching { getCurrentPhaseUseCase() }
                .getOrDefault(CyclePhase.UNKNOWN to 0)
            val recent = runCatching { getRecentLogSummaryUseCase() }.getOrNull()

            val history = _uiState.value.messages
                .filter { !it.isStreaming && it.id != userDbId }
                .map { ChatTurn(isUser = it.isUser, text = it.text) }

            streamJob?.cancel()
            streamJob = launch {
                val buffer = StringBuilder()
                voiceCompanionOrchestrator.ask(
                    userQuestion = trimmed,
                    history = history,
                    cyclePhase = phase,
                    cycleDayNumber = dayInCycle,
                    recent = recent,
                ).collect { event ->
                    _voiceEvents.tryEmit(event)
                    _currentEmotion.value = event.emotion
                    when (event) {
                        is VoiceCompanionEvent.SlowDelta -> {
                            buffer.append(event.text)
                            updatePlaceholder(streamingLocalId, buffer.toString(), streaming = true)
                        }
                        is VoiceCompanionEvent.SlowDone -> {
                            val assistantDbId = chatHistoryRepository.addMessage(
                                isUser = false, text = event.fullText,
                            )
                            _uiState.value = _uiState.value.copy(
                                messages = _uiState.value.messages.map {
                                    if (it.id == streamingLocalId) {
                                        it.copy(id = assistantDbId, text = event.fullText, isStreaming = false)
                                    } else it
                                },
                                isSending = false,
                            )
                        }
                        is VoiceCompanionEvent.Error -> {
                            removePlaceholder(streamingLocalId)
                            _uiState.value = _uiState.value.copy(
                                isSending = false,
                                errorMessage = event.message,
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun clearHistoryImpl() {
        // Intentionally empty — kept as an anchor for future admin flows.
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun updatePlaceholder(id: Long, text: String, streaming: Boolean) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages.map {
                if (it.id == id) it.copy(text = text, isStreaming = streaming) else it
            }
        )
    }

    private fun removePlaceholder(id: Long) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages.filterNot { it.id == id }
        )
    }
}
