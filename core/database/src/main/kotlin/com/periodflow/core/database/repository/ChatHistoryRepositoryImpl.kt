package com.periodflow.core.database.repository

import com.periodflow.core.database.dao.ChatMessageDao
import com.periodflow.core.database.entity.ChatMessageEntity
import com.periodflow.core.domain.repository.ChatHistoryRepository
import com.periodflow.core.domain.repository.ChatMessageRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatHistoryRepositoryImpl @Inject constructor(
    private val dao: ChatMessageDao,
) : ChatHistoryRepository {

    override fun observeMessages(): Flow<List<ChatMessageRecord>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getMessages(): List<ChatMessageRecord> =
        dao.getAll().map { it.toDomain() }

    override suspend fun addMessage(isUser: Boolean, text: String): Long =
        dao.insert(
            ChatMessageEntity(
                isUser = isUser,
                text = text,
                createdAtEpochMilli = System.currentTimeMillis(),
            )
        )

    override suspend fun updateMessage(id: Long, text: String) {
        // Fetch-then-update pattern would be cleaner, but we only need to change `text`.
        // Room's @Update needs the full entity, so we reconstruct it. Since text is the only
        // field the UI mutates for streaming placeholders, we preserve the original timestamp.
        val existing = dao.getAll().firstOrNull { it.id == id } ?: return
        dao.update(existing.copy(text = text))
    }

    override suspend fun clear() {
        dao.clear()
    }

    private fun ChatMessageEntity.toDomain() = ChatMessageRecord(
        id = id, isUser = isUser, text = text, createdAtEpochMilli = createdAtEpochMilli,
    )
}
