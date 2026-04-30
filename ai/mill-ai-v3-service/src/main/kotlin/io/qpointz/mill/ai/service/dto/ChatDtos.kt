package io.qpointz.mill.ai.service.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.qpointz.mill.ai.service.CreateChatRequest
import io.qpointz.mill.ai.service.ChatView
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ChatUpdate
import io.qpointz.mill.ai.persistence.ConversationTurn

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
) {
    fun toChatUpdate() = ChatUpdate(
        chatName = chatName,
        isFavorite = isFavorite,
        contextLabel = contextLabel,
    )
}

/** POST /api/v1/ai/chats/{chatId}/messages */
data class SendMessageHttpRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @JsonProperty("message")
    val message: String,
)

// ── Outbound ──────────────────────────────────────────────────────────────────

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
            messages = view.messages.map(TurnResponse::from),
        )
    }
}

/** Single durable conversation turn. */
data class TurnResponse(
    val turnId: String,
    val role: String,
    val text: String?,
    val createdAt: String,
) {
    companion object {
        fun from(t: ConversationTurn) = TurnResponse(
            turnId = t.turnId,
            role = t.role,
            text = t.text,
            createdAt = t.createdAt.toString(),
        )
    }
}
