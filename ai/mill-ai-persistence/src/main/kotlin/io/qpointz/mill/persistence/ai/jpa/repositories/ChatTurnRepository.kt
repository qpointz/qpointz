package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ChatTurnEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatTurnRepository : JpaRepository<ChatTurnEntity, String> {
    fun findByChatIdOrderByPositionAsc(chatId: String): List<ChatTurnEntity>

    fun countByChatId(chatId: String): Long

    fun deleteByChatId(chatId: String)
}
