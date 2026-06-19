package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.chat.AiChatSettings
import io.qpointz.mill.ai.chat.UserIdResolver
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ChatUpdate
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.service.dto.AttachExecutionResultHttpRequest
import io.qpointz.mill.ai.service.dto.ArtifactResponse
import io.qpointz.mill.ai.service.dto.TurnResponse
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
 * @param artifactStore durable structured artefact rows for GET replay
 * @param profileRegistry agent profile catalog for profile switch validation
 * @param runtime pluggable agent execution; replace to swap LLM providers or use a stub
 * @param properties chat service settings (from `mill.ai.chat` in Spring hosts)
 * @param userIdResolver extension point for resolving the current user
 */
class UnifiedChatService(
    private val registry: ChatRegistry,
    private val conversationStore: ConversationStore,
    private val chatMemoryStore: ChatMemoryStore,
    private val artifactStore: ArtifactStore,
    private val profileRegistry: ProfileRegistry,
    private val runtime: AiV3ChatRuntime,
    private val properties: AiChatSettings,
    private val userIdResolver: UserIdResolver,
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
        val metadata = requireOwned(registry.load(chatId)) ?: return null
        val record = conversationStore.load(chatId)
        val turns = record?.turns ?: emptyList()
        return ChatView(
            chat = metadata,
            messages = mapTurnResponses(chatId, turns),
        )
    }

    /**
     * Persists client-side query execution metadata on a turn (no server SQL execution).
     *
     * @param chatId chat identifier
     * @param turnId durable turn identifier
     * @param request execution metadata from mill-ui `queryService`
     * @return attached wire artefact, or `null` when chat/turn is missing
     */
    override fun attachExecutionResult(
        chatId: String,
        turnId: String,
        request: AttachExecutionResultHttpRequest,
    ): ArtifactResponse? {
        val metadata = requireOwned(registry.load(chatId)) ?: return null
        val record = conversationStore.load(chatId) ?: return null
        if (record.turns.none { it.turnId == turnId }) return null

        val artifactId = UUID.randomUUID().toString()
        val payload = mapOf(
            "artifactType" to "sql-result",
            "executionId" to request.executionId,
            "resultId" to request.executionId,
            "sql" to request.sql,
            "rowCount" to request.rowCount,
            "truncated" to request.truncated,
            "columns" to request.columns.map { mapOf("name" to it.name, "type" to it.type) },
        )
        artifactStore.save(
            ArtifactRecord(
                artifactId = artifactId,
                conversationId = chatId,
                runId = null,
                kind = "sql.result",
                payload = payload,
                turnId = turnId,
                pointerKeys = setOf("last-sql-result"),
                createdAt = Instant.now(),
            ),
        )
        conversationStore.attachArtifacts(chatId, turnId, listOf(artifactId))
        return ArtifactWireMapper.toResponse(
            artifactStore.findById(artifactId) ?: return null,
        )
    }

    private fun mapTurnResponses(chatId: String, turns: List<ConversationTurn>): List<TurnResponse> {
        val conversationArtifacts = artifactStore.findByConversation(chatId)
        val byId = conversationArtifacts.associateBy { it.artifactId }
        val byTurnId = conversationArtifacts
            .filter { it.turnId != null }
            .groupBy { it.turnId!! }
        return turns.map { turn ->
            val linkedIds = when {
                turn.artifactIds.isNotEmpty() -> turn.artifactIds
                else -> byTurnId[turn.turnId].orEmpty().map { it.artifactId }
            }
            val artifacts = linkedIds
                .mapNotNull { byId[it] }
                .mapNotNull { ArtifactWireMapper.toResponse(it) }
            TurnResponse.from(turn, artifacts)
        }
    }

    /** Returns a context-bound chat by `(contextType, contextId)`, or `null`. */
    override fun getChatByContext(contextType: String, contextId: String): ChatMetadata? =
        requireOwned(registry.findByContext(userIdResolver.resolve(), contextType, contextId))

    /**
     * Updates mutable chat fields (name, favourite flag, context label, profile on general chats).
     * Returns the updated metadata, or `null` if the chat does not exist.
     *
     * @throws InvalidChatUpdateException when [ChatUpdate.profileId] is invalid or not allowed
     */
    override fun updateChat(chatId: String, update: ChatUpdate): ChatMetadata? {
        val existing = requireOwned(registry.load(chatId)) ?: return null
        validateProfileUpdate(existing, update.profileId)
        val updated = registry.update(chatId, update) ?: return null
        val nextProfileId = update.profileId
        if (nextProfileId != null && nextProfileId != existing.profileId) {
            conversationStore.updateProfileId(chatId, nextProfileId)
        }
        return updated
    }

    private fun validateProfileUpdate(existing: ChatMetadata, profileId: String?) {
        if (profileId == null || profileId == existing.profileId) return
        if (existing.chatType != "general") {
            throw InvalidChatUpdateException("Profile cannot be changed on contextual chats")
        }
        if (profileRegistry.resolve(profileId) == null) {
            throw InvalidChatUpdateException("Unknown profile: $profileId")
        }
    }

    /**
     * Hard-deletes the chat — metadata, durable transcript, and LLM memory are all removed.
     *
     * Returns `true` when the chat existed and was removed.
     * Artifact and run-event cleanup are handled at the persistence layer (JPA cascade or
     * a background eviction job for in-memory stores).
     */
    override fun deleteChat(chatId: String): Boolean {
        if (requireOwned(registry.load(chatId)) == null) return false
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
        val metadata = requireOwned(registry.load(chatId))
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

    /**
     * Returns [metadata] when it belongs to the current user; otherwise `null` (404 at HTTP layer).
     */
    private fun requireOwned(metadata: ChatMetadata?): ChatMetadata? {
        if (metadata == null) return null
        return if (metadata.userId == userIdResolver.resolve()) metadata else null
    }
}
