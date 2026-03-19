package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ConversationTurnEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConversationTurnRepository : JpaRepository<ConversationTurnEntity, String> {
    fun findByConversationIdOrderByPositionAsc(conversationId: String): List<ConversationTurnEntity>
    fun countByConversationId(conversationId: String): Long
}
