package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.service.dto.TurnResponse

/**
 * Enriched chat response returned by [UnifiedChatService.getChat].
 *
 * Combines [ChatMetadata] (identity + mutable fields) with wire-mapped durable turns
 * ([messages]) including replay artefacts when present.
 */
data class ChatView(
    val chat: ChatMetadata,
    val messages: List<TurnResponse> = emptyList(),
)
