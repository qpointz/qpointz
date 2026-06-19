package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<ChatEntity, String> {
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<ChatEntity>

    fun findByUserIdAndContextTypeAndContextId(
        userId: String,
        contextType: String,
        contextId: String,
    ): ChatEntity?
}
