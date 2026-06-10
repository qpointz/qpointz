package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ConversationTurn

/**
 * Enriched chat response returned by [UnifiedChatService.getChat].
 *
 * Combines [ChatMetadata] (identity + mutable fields) with the durable conversation
 * transcript ([messages]) loaded from [io.qpointz.mill.ai.persistence.ConversationStore].
 */
data class ChatView(
    val chat: ChatMetadata,
    val messages: List<ConversationTurn> = emptyList(),
)
