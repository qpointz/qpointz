package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatRegistry
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatRepository
import java.time.Instant

/** Seeds an `ai_chat` parent row required by V12 FK constraints in integration tests. */
object JpaChatTestSupport {

    fun seedChat(
        chatRepo: ChatRepository,
        chatId: String,
        profileId: String = "test-profile",
        userId: String = "test-user",
    ) {
        if (chatRepo.existsById(chatId)) return
        val now = Instant.now()
        JpaChatRegistry(chatRepo).create(
            ChatMetadata(
                chatId = chatId,
                userId = userId,
                profileId = profileId,
                chatName = "Test Chat",
                chatType = "general",
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}
