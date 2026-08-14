package com.periodflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted Bloom chat message.
 * `createdAtEpochMilli` is used to sort chronologically. Not tied to any user
 * account — the app is offline-first / single-user by design.
 */
@Entity(tableName = "chat_message")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isUser: Boolean,
    val text: String,
    val createdAtEpochMilli: Long,
)
