package com.periodflow.core.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persistent Bloom chat history (offline, single-user).
 * Messages are ordered chronologically by `createdAtEpochMilli`.
 */
interface ChatHistoryRepository {
    fun observeMessages(): Flow<List<ChatMessageRecord>>
    suspend fun getMessages(): List<ChatMessageRecord>
    /** Returns the auto-generated row id. */
    suspend fun addMessage(isUser: Boolean, text: String): Long
    /** Update the text of an existing message (used to persist streamed assistant text as it finalises). */
    suspend fun updateMessage(id: Long, text: String)
    suspend fun clear()
}

data class ChatMessageRecord(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val createdAtEpochMilli: Long,
)
