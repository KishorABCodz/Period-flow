package com.periodflow.core.ai.model

/**
 * Streaming event emitted by AI repositories.
 * ViewModels concatenate `Delta.text` into a single narrative until `Done` (or `Error`).
 */
sealed interface AiStreamEvent {
    data class Delta(val text: String) : AiStreamEvent
    data object Done : AiStreamEvent
    data class Error(val message: String) : AiStreamEvent
}
