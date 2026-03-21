package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.persistence.ChatMetadata

/**
 * Result of [UnifiedChatService.createChat].
 *
 * [created] is `true` when a new chat was just inserted, `false` when an existing
 * contextual singleton was returned. The controller uses this to pick the correct
 * HTTP status: `201 Created` vs `200 OK`.
 */
data class ChatCreationResult(
    val chat: ChatMetadata,
    val created: Boolean,
)
