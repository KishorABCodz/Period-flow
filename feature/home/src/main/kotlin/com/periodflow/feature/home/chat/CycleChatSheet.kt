package com.periodflow.feature.home.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodflow.core.ai.voice.VoiceCompanionEvent
import com.periodflow.core.ui.components.ClayButton
import com.periodflow.core.ui.components.claymorphism
import com.periodflow.feature.home.chat.voice.BloomTts
import com.periodflow.feature.home.chat.voice.VoiceInputController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleChatSheet(
    onDismiss: () -> Unit,
    viewModel: CycleChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val voiceModeEnabled by viewModel.voiceModeEnabled.collectAsStateWithLifecycle()
    val currentEmotion by viewModel.currentEmotion.collectAsStateWithLifecycle()
    val hasGemmaModel by viewModel.hasGemmaModel.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) },
    ) {
        ChatContent(
            state = uiState,
            voiceEvents = viewModel.voiceEvents,
            voiceModeEnabled = voiceModeEnabled,
            currentEmotion = currentEmotion,
            hasGemmaModel = hasGemmaModel,
            downloadProgress = downloadProgress,
            onSendText = viewModel::sendMessage,
            onSendVoice = viewModel::sendVoiceMessage,
            onToggleVoiceMode = viewModel::setVoiceModeEnabled,
            onStartModelDownload = { viewModel.startModelDownload() },
            onDismissDownload = viewModel::dismissDownloadState,
            onClearError = viewModel::clearError,
            onClearHistory = viewModel::clearHistory,
        )
    }
}

@Composable
private fun ChatContent(
    state: CycleChatUiState,
    voiceEvents: kotlinx.coroutines.flow.SharedFlow<VoiceCompanionEvent>,
    voiceModeEnabled: Boolean,
    currentEmotion: com.periodflow.core.ai.voice.BloomEmotion,
    hasGemmaModel: Boolean,
    downloadProgress: com.periodflow.feature.home.chat.voice.ModelDownloader.DownloadProgress?,
    onSendText: (String) -> Unit,
    onSendVoice: (String) -> Unit,
    onToggleVoiceMode: (Boolean) -> Unit,
    onStartModelDownload: () -> Unit,
    onDismissDownload: () -> Unit,
    onClearError: () -> Unit,
    onClearHistory: () -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // TTS engine — reused across sheet lifetime, emotion-aware.
    val bloomTts = remember { BloomTts(context) }
    DisposableEffect(Unit) { onDispose { bloomTts.shutdown() } }

    // Speak the voice orchestrator's fast + slow events when voice mode is on.
    LaunchedEffect(voiceModeEnabled) {
        if (!voiceModeEnabled) return@LaunchedEffect
        voiceEvents.collect { event ->
            when (event) {
                is VoiceCompanionEvent.FastGreeting -> bloomTts.speakEmotive(event.text, event.emotion, isFiller = true)
                is VoiceCompanionEvent.FastThinking -> bloomTts.speakEmotive(event.text, event.emotion, isFiller = true)
                is VoiceCompanionEvent.SlowDone -> bloomTts.speakEmotive(event.fullText, event.emotion, isFiller = false)
                else -> Unit
            }
        }
    }

    // Voice input state
    var isListening by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }

    val voiceController = remember {
        VoiceInputController(
            context = context,
            onPartial = { partial -> draft = partial },
            onFinal = { finalText -> draft = finalText },
            onError = { msg -> voiceError = msg },
            onStateChanged = { listening -> isListening = listening },
        )
    }

    // Release the platform recognizer when the sheet leaves composition.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) voiceController.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceController.release()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) voiceController.start()
        else voiceError = "Microphone permission is required to talk to Bloom."
    }

    fun toggleMic() {
        voiceError = null
        if (isListening) voiceController.stop()
        else if (voiceController.hasPermission()) voiceController.start()
        else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp, max = 640.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Text(
                text = "Bloom · Cycle Chat",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = currentEmotion.glyph,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 4.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    val next = !voiceModeEnabled
                    onToggleVoiceMode(next)
                    if (!next) bloomTts.stop()
                    if (next && !hasGemmaModel) onStartModelDownload()
                },
            ) {
                Icon(
                    imageVector = if (voiceModeEnabled) Icons.Rounded.RecordVoiceOver else Icons.Rounded.VolumeOff,
                    contentDescription = if (voiceModeEnabled) "Voice mode on" else "Voice mode off",
                    tint = if (voiceModeEnabled) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onClearHistory) {
                Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = "Clear chat history",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Gemma model download banner
        downloadProgress?.let { progress ->
            ModelDownloadBanner(progress = progress, onDismiss = onDismissDownload)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        // Error banner
        state.errorMessage?.let { err ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClearError) {
                    Text(text = "Dismiss", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(state.messages, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
        }

        // Voice error banner
        voiceError?.let { err ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { voiceError = null }) {
                    Text(text = "OK", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Composer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (isListening) "Listening…" else "Ask about your cycle…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                shape = RoundedCornerShape(28.dp),
                enabled = !state.isSending,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
            // Mic button
            if (voiceController.isAvailable) {
                ClayButton(
                    onClick = { toggleMic() },
                    backgroundColor = if (isListening) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Icon(
                        imageVector = when {
                            isListening -> Icons.Rounded.Stop
                            draft.isBlank() -> Icons.Rounded.MicNone
                            else -> Icons.Rounded.Mic
                        },
                        contentDescription = if (isListening) "Stop listening" else "Speak to Bloom",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            // Send button
            ClayButton(
                onClick = {
                    val toSend = draft
                    draft = ""
                    if (voiceModeEnabled) onSendVoice(toSend) else onSendText(toSend)
                },
                backgroundColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(28.dp),
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    val shape = if (isUser)
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
    else
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .claymorphism(backgroundColor = bubbleColor, shape = shape, elevation = 4.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            val caret = if (msg.isStreaming) " ▍" else ""
            Text(
                text = msg.text + caret,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun ModelDownloadBanner(
    progress: ModelDownloader.DownloadProgress,
    onDismiss: () -> Unit,
) {
    val (title, subtitle, ratio, isError) = when (progress) {
        is ModelDownloader.DownloadProgress.Running -> {
            val mb = (progress.downloadedBytes / (1024 * 1024)).toInt()
            val totalMb = (progress.totalBytes / (1024 * 1024)).toInt().coerceAtLeast(0)
            Quadruple(
                "Downloading Bloom's voice model…",
                if (totalMb > 0) "$mb MB of $totalMb MB" else "$mb MB so far",
                progress.ratio,
                false,
            )
        }
        ModelDownloader.DownloadProgress.Success ->
            Quadruple("Voice model ready", "Bloom will speak with warmth from now on.", 1f, false)
        is ModelDownloader.DownloadProgress.Failed ->
            Quadruple("Model download failed", progress.reason, 0f, true)
    }

    val bg = if (isError) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    val fg = if (isError) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .claymorphism(backgroundColor = bg, shape = RoundedCornerShape(16.dp), elevation = 4.dp)
            .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = fg)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = fg)
            }
            TextButton(onClick = onDismiss) { Text("Dismiss", color = fg) }
        }
        if (progress is ModelDownloader.DownloadProgress.Running) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
