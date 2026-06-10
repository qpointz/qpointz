package io.qpointz.mill.ai.persistence

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class InMemoryChatRegistry : ChatRegistry {

    private val store = ConcurrentHashMap<String, ChatMetadata>()

    override fun create(metadata: ChatMetadata): ChatMetadata {
        store[metadata.chatId] = metadata
        return metadata
    }

    override fun load(chatId: String): ChatMetadata? = store[chatId]

    override fun list(userId: String): List<ChatMetadata> =
        store.values.filter { it.userId == userId }.sortedByDescending { it.createdAt }

    override fun update(chatId: String, update: ChatUpdate): ChatMetadata? =
        store.computeIfPresent(chatId) { _, existing ->
            existing.copy(
                chatName = update.chatName ?: existing.chatName,
                isFavorite = update.isFavorite ?: existing.isFavorite,
                contextLabel = update.contextLabel ?: existing.contextLabel,
                updatedAt = Instant.now(),
            )
        }

    override fun delete(chatId: String): Boolean =
        store.remove(chatId) != null

    override fun findByContext(userId: String, contextType: String, contextId: String): ChatMetadata? =
        store.values.firstOrNull {
            it.userId == userId && it.contextType == contextType && it.contextId == contextId
        }
}
