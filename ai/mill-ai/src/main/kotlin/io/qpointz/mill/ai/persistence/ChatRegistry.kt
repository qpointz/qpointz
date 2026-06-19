package io.qpointz.mill.ai.persistence

import java.time.Instant

/**
 * Mutable fields that callers are allowed to update on an existing chat.
 *
 * Identity and ownership fields (chatId, userId, context keys) are immutable after creation.
 * [profileId] may be updated for general chats only; the service layer enforces that rule.
 */
data class ChatUpdate(
    val chatName: String? = null,
    val isFavorite: Boolean? = null,
    val contextLabel: String? = null,
    val profileId: String? = null,
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
 * Delete semantics: JPA adapters rely on `ON DELETE CASCADE` from `ai_chat` to satellite
 * tables (`ai_chat_turn`, `ai_chat_memory`, `ai_chat_artifact`, `ai_chat_run_event`, etc.).
 * The service layer still invokes transcript/memory cleanup for in-memory backends.
 *
 * Identity and ownership fields (chatId, userId, context keys) are immutable after creation.
 * [profileId] is mutable for general chats via [ChatUpdate] (validated in the service layer).
 */
interface ChatRegistry {
    fun create(metadata: ChatMetadata): ChatMetadata
    fun load(chatId: String): ChatMetadata?
    fun list(userId: String): List<ChatMetadata>
    fun update(chatId: String, update: ChatUpdate): ChatMetadata?
    fun delete(chatId: String): Boolean
    fun findByContext(userId: String, contextType: String, contextId: String): ChatMetadata?
}
