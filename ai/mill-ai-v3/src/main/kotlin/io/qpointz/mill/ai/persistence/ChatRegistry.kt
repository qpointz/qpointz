package io.qpointz.mill.ai.persistence

import java.time.Instant

/**
 * Mutable fields that callers are allowed to update on an existing chat.
 * Identity and ownership fields (chatId, userId, profileId, context keys) are immutable
 * after creation and are never touched by [ChatRegistry.update].
 */
data class ChatUpdate(
    val chatName: String? = null,
    val isFavorite: Boolean? = null,
    val contextLabel: String? = null,
)

data class ChatMetadata(
    val chatId: String,
    val userId: String,
    val profileId: String,
    val chatName: String,
    val chatType: String,
    val isFavorite: Boolean = false,
    val contextType: String? = null,
    val contextId: String? = null,
    val contextLabel: String? = null,
    val contextEntityType: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/**
 * Port for persisted chat metadata.
 *
 * Separate from [ConversationStore] (transcript) and [io.qpointz.mill.ai.memory.ChatMemoryStore]
 * (LLM context window). This store is the authority for chat listing, naming, ownership,
 * favorites, context binding, and runtime rehydration inputs.
 *
 * Delete semantics are hard-delete on the metadata row only. Cascade cleanup of associated
 * transcript, memory, artifacts, and pointers is the responsibility of the service layer.
 *
 * Identity and ownership fields (chatId, userId, profileId, context keys) are immutable
 * after creation. Use [ChatUpdate] to restrict mutations to safe mutable fields only.
 */
interface ChatRegistry {
    fun create(metadata: ChatMetadata): ChatMetadata
    fun load(chatId: String): ChatMetadata?
    fun list(userId: String): List<ChatMetadata>
    fun update(chatId: String, update: ChatUpdate): ChatMetadata?
    fun delete(chatId: String): Boolean
    fun findByContext(userId: String, contextType: String, contextId: String): ChatMetadata?
}
