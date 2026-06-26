package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ChatUpdate
import io.qpointz.mill.ai.service.dto.ArtifactResponse
import io.qpointz.mill.ai.service.dto.AttachExecutionResultHttpRequest
import reactor.core.publisher.Flux

/**
 * Primary service boundary for the unified AI v3 chat API.
 *
 * Controllers depend on this interface; [UnifiedChatService] is the production implementation.
 * Override or replace to adapt for multi-tenancy, additional business rules, or testing.
 */
interface ChatService {

    /** Returns general (non-contextual) chats for the current user, newest first. */
    fun listChats(): List<ChatMetadata>

    /**
     * Creates a new chat or returns an existing singleton for the same context.
     *
     * [ChatCreationResult.created] is `true` when a new chat was created, `false` when
     * an existing contextual singleton was reused.
     */
    fun createChat(request: CreateChatRequest?): ChatCreationResult

    /** Returns the chat and its durable message transcript, or `null` if not found. */
    fun getChat(chatId: String): ChatView?

    /** Returns a context-bound chat by `(contextType, contextId)`, or `null`. */
    fun getChatByContext(contextType: String, contextId: String): ChatMetadata?

    /**
     * Updates mutable chat fields (name, favourite flag, context label, profile on general chats).
     * Returns the updated metadata, or `null` if the chat does not exist.
     *
     * @throws InvalidChatUpdateException when profile update is invalid (mapped to HTTP 400)
     */
    fun updateChat(chatId: String, update: ChatUpdate): ChatMetadata?

    /**
     * Hard-deletes the chat — metadata, durable transcript, and LLM memory are all removed.
     * Returns `true` when the chat existed and was removed.
     */
    fun deleteChat(chatId: String): Boolean

    /** Sends a user message and returns a streaming [Flux] of [ChatRuntimeEvent]s. */
    fun sendMessage(chatId: String, message: String): Flux<ChatRuntimeEvent>

    /**
     * Persists client-side query execution metadata on a turn (no server SQL execution).
     *
     * @param chatId chat identifier
     * @param turnId durable turn identifier
     * @param request execution metadata from mill-ui `queryService`
     * @return attached wire artefact, or `null` when chat/turn is missing
     */
    fun attachExecutionResult(
        chatId: String,
        turnId: String,
        request: AttachExecutionResultHttpRequest,
    ): ArtifactResponse?

    /**
     * Accepts a pending facet-proposal artefact.
     *
     * @param chatId owning chat id
     * @param artifactId persisted artefact id
     */
    fun acceptArtifact(chatId: String, artifactId: String): ArtifactResponse?

    /**
     * Rejects a pending facet-proposal artefact.
     *
     * @param chatId owning chat id
     * @param artifactId persisted artefact id
     */
    fun rejectArtifact(chatId: String, artifactId: String): Boolean
}
