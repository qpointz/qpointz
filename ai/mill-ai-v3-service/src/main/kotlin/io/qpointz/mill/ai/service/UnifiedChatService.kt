package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.autoconfigure.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.autoconfigure.chat.MillAiV3ChatProperties
import io.qpointz.mill.ai.autoconfigure.chat.PropertiesUserIdResolver
import io.qpointz.mill.ai.autoconfigure.chat.UserIdResolver
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ChatUpdate
import io.qpointz.mill.ai.persistence.ConversationStore
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.UUID

/**
 * Orchestration boundary for the unified AI v3 chat API.
 *
 * Keeps all business rules (contextual-chat reuse, title derivation, conversation
 * bootstrap, hard-delete cascade) out of the HTTP layer. The controller delegates
 * here; this class delegates to [ChatRegistry], [ConversationStore], [ChatMemoryStore],
 * and [AiV3ChatRuntime].
 *
 * @param registry metadata store for chat lifecycle operations
 * @param conversationStore durable turn transcript
 * @param chatMemoryStore LLM-facing sliding-window memory
 * @param runtime pluggable agent execution; replace to swap LLM providers or use a stub
 * @param properties chat service configuration
 * @param userIdResolver extension point for resolving the current user;
 *   defaults to the static value from [MillAiV3ChatProperties.defaultUserId]
 */
class UnifiedChatService(
    private val registry: ChatRegistry,
    private val conversationStore: ConversationStore,
    private val chatMemoryStore: ChatMemoryStore,
    private val runtime: AiV3ChatRuntime,
    private val properties: MillAiV3ChatProperties,
    private val userIdResolver: UserIdResolver = PropertiesUserIdResolver(properties.defaultUserId),
) : ChatService {

    // ── Chat lifecycle ─────────────────────────────────────────────────────────

    /**
     * Creates a new chat or returns an existing one for the same context.
     *
     * - **General chat** (`request == null` or no context fields): always creates a new chat.
     * - **Contextual chat**: if a chat already exists for
     *   `(userId, contextType, contextId)` it is returned unchanged ([ChatCreationResult.created] = `false`).
     */
    override fun createChat(request: CreateChatRequest?): ChatCreationResult {
        val userId = userIdResolver.resolve()
        val profileId = request?.profileId ?: properties.defaultProfile

        // Contextual singleton: reuse an existing chat for the same context
        if (request?.contextType != null && request.contextId != null) {
            val existing = registry.findByContext(userId, request.contextType, request.contextId)
            if (existing != null) return ChatCreationResult(chat = existing, created = false)
        }

        val now = Instant.now()
        val metadata = ChatMetadata(
            chatId = UUID.randomUUID().toString(),
            userId = userId,
            profileId = profileId,
            chatName = deriveName(request),
            chatType = if (request?.contextType != null) "contextual" else "general",
            isFavorite = false,
            contextType = request?.contextType,
            contextId = request?.contextId,
            contextLabel = request?.contextLabel,
            contextEntityType = request?.contextEntityType,
            createdAt = now,
            updatedAt = now,
        )
        return ChatCreationResult(chat = registry.create(metadata), created = true)
    }

    /**
     * Returns general (non-contextual) chats owned by the current user, newest first.
     *
     * Contextual chats are excluded from this list; they are accessed via
     * [getChatByContext] or the context-lookup endpoint.
     */
    override fun listChats(): List<ChatMetadata> =
        registry.list(userIdResolver.resolve()).filter { it.chatType == "general" }

    /** Returns the chat and its durable message transcript, or `null` if not found. */
    override fun getChat(chatId: String): ChatView? {
        val metadata = registry.load(chatId) ?: return null
        val record = conversationStore.load(chatId)
        return ChatView(chat = metadata, messages = record?.turns ?: emptyList())
    }

    /** Returns a context-bound chat by `(contextType, contextId)`, or `null`. */
    override fun getChatByContext(contextType: String, contextId: String): ChatMetadata? =
        registry.findByContext(userIdResolver.resolve(), contextType, contextId)

    /**
     * Updates mutable chat fields (name, favourite flag).
     * Returns the updated metadata, or `null` if the chat does not exist.
     */
    override fun updateChat(chatId: String, update: ChatUpdate): ChatMetadata? =
        registry.update(chatId, update)

    /**
     * Hard-deletes the chat — metadata, durable transcript, and LLM memory are all removed.
     *
     * Returns `true` when the chat existed and was removed.
     * Artifact and run-event cleanup are handled at the persistence layer (JPA cascade or
     * a background eviction job for in-memory stores).
     */
    override fun deleteChat(chatId: String): Boolean {
        val deleted = registry.delete(chatId)
        if (deleted) {
            conversationStore.delete(chatId)
            chatMemoryStore.clear(chatId)
        }
        return deleted
    }

    // ── Messaging ──────────────────────────────────────────────────────────────

    /**
     * Sends a user message and returns a streaming [Flux] of [ChatRuntimeEvent]s.
     *
     * Also handles first-message title derivation: if the chat name is still the
     * placeholder `"New Chat"`, it is updated to a truncated version of [message].
     */
    override fun sendMessage(chatId: String, message: String): Flux<ChatRuntimeEvent> {
        val metadata = registry.load(chatId)
            ?: return Flux.error(NoSuchElementException("Chat not found: $chatId"))

        conversationStore.ensureExists(chatId, metadata.profileId)

        if (metadata.chatName == "New Chat") {
            val title = message.take(properties.maxTitleLength).let {
                if (message.length > properties.maxTitleLength) "$it..." else it
            }
            registry.update(chatId, ChatUpdate(chatName = title))
        }

        return runtime.send(metadata, message)
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun deriveName(request: CreateChatRequest?): String =
        request?.contextLabel ?: "New Chat"
}
