package io.qpointz.mill.ai.service

/**
 * Raised when a chat metadata update violates service rules (unknown profile, contextual chat, etc.).
 *
 * Mapped to HTTP 400 by [AiChatController].
 */
class InvalidChatUpdateException(message: String) : RuntimeException(message)
