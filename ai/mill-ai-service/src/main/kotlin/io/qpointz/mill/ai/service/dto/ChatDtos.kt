package io.qpointz.mill.ai.service.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.qpointz.mill.ai.service.CreateChatRequest
import io.qpointz.mill.ai.service.ChatView
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ChatUpdate
import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.ai.service.ArtifactWireMapper

// ── Inbound ───────────────────────────────────────────────────────────────────

/** POST /api/v1/ai/chats */
data class CreateChatHttpRequest(
    val profileId: String? = null,
    val contextType: String? = null,
    val contextId: String? = null,
    val contextLabel: String? = null,
    val contextEntityType: String? = null,
) {
    fun toServiceRequest() = CreateChatRequest(
        profileId = profileId,
        contextType = contextType,
        contextId = contextId,
        contextLabel = contextLabel,
        contextEntityType = contextEntityType,
    )
}

/** PATCH /api/v1/ai/chats/{chatId} */
data class UpdateChatHttpRequest(
    val chatName: String? = null,
    val isFavorite: Boolean? = null,
    val contextLabel: String? = null,
    val profileId: String? = null,
) {
    fun toChatUpdate() = ChatUpdate(
        chatName = chatName,
        isFavorite = isFavorite,
        contextLabel = contextLabel,
        profileId = profileId,
    )
}

/** POST /api/v1/ai/chats/{chatId}/messages */
data class SendMessageHttpRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @JsonProperty("message")
    val message: String,
)

/** POST /api/v1/ai/chats/{chatId}/turns/{turnId}/execution-result — attach client query metadata only. */
data class AttachExecutionResultHttpRequest(
    val executionId: String,
    val columns: List<ExecutionColumnDto> = emptyList(),
    val rowCount: Long = 0,
    val truncated: Boolean? = null,
    val sql: String? = null,
    /** Parent `sql` artefact id when multiple SQL cards exist on one turn. */
    val parentArtifactId: String? = null,
)

/** Column descriptor in attach-result body. */
data class ExecutionColumnDto(
    val name: String,
    val type: String,
)

// ── Outbound ──────────────────────────────────────────────────────────────────

/** Consumer-safe structured artefact on a durable turn (GET replay). */
data class ArtifactResponse(
    /** Wire kind aligned with SSE `partType` (`sql`, `data`, `facet-proposal`). */
    val kind: String,
    val payload: Map<String, Any?>,
    /** Opaque persisted artefact id for Accept/Reject and sql↔data pairing. */
    val artifactId: String? = null,
    /** Canonical artefact URN when [artifactId] is present. */
    val urn: String? = null,
    /** Operator lifecycle (`pending`, `accepted`, `retracted`). */
    val status: String? = null,
)

/** Chat metadata response (list + create + update). */
data class ChatResponse(
    val chatId: String,
    val userId: String,
    val profileId: String,
    val chatName: String,
    val chatType: String,
    val isFavorite: Boolean,
    val contextType: String?,
    val contextId: String?,
    val contextLabel: String?,
    val contextEntityType: String?,
    val createdAt: String,
    val updatedAt: String,
) {
    companion object {
        fun from(m: ChatMetadata) = ChatResponse(
            chatId = m.chatId,
            userId = m.userId,
            profileId = m.profileId,
            chatName = m.chatName,
            chatType = m.chatType,
            isFavorite = m.isFavorite,
            contextType = m.contextType,
            contextId = m.contextId,
            contextLabel = m.contextLabel,
            contextEntityType = m.contextEntityType,
            createdAt = m.createdAt.toString(),
            updatedAt = m.updatedAt.toString(),
        )
    }
}

/** Chat detail response (GET /{chatId}). */
data class ChatDetailResponse(
    val chat: ChatResponse,
    val messages: List<TurnResponse>,
) {
    companion object {
        fun from(view: ChatView) = ChatDetailResponse(
            chat = ChatResponse.from(view.chat),
            messages = view.messages,
        )
    }
}

/**
 * Single durable conversation turn.
 *
 * @param artifacts Structured artefacts linked to the turn for GET replay.
 * @param assistantReplyView Optional mill-ui layout hint; omitted on GET replay (client derives from artefacts).
 */
data class TurnResponse(
    val turnId: String,
    val role: String,
    val text: String?,
    val profileId: String,
    val createdAt: String,
    val artifacts: List<ArtifactResponse> = emptyList(),
    val assistantReplyView: String? = null,
) {
    companion object {
        /**
         * Maps a durable turn plus pre-resolved wire artefacts.
         *
         * @param t conversation turn row
         * @param artifacts consumer-safe artefacts for [t]
         */
        fun from(t: ConversationTurn, artifacts: List<ArtifactResponse> = emptyList()) = TurnResponse(
            turnId = t.turnId,
            role = t.role,
            text = t.text,
            profileId = t.profileId,
            createdAt = t.createdAt.toString(),
            artifacts = artifacts,
            assistantReplyView = null,
        )
    }
}
