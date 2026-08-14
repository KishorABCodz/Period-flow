package com.periodflow.core.ai.model

/**
 * Result wrapper for AI-generated content.
 * Kept minimal and screen-friendly so ViewModels can map directly to UI states.
 */
sealed interface AiResult<out T> {
    data object Idle : AiResult<Nothing>
    data object Loading : AiResult<Nothing>
    data class Success<T>(val value: T) : AiResult<T>
    data class Error(val message: String) : AiResult<Nothing>
}
