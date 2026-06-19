package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ChatMemoryMessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMemoryMessageRepository : JpaRepository<ChatMemoryMessageEntity, Long> {
    fun deleteByChatId(chatId: String)

    fun findByChatIdOrderByPositionAsc(chatId: String): List<ChatMemoryMessageEntity>
}
