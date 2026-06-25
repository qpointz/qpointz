package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ActiveArtifactPointerEntity
import io.qpointz.mill.persistence.ai.jpa.entities.ActiveArtifactPointerKey
import org.springframework.data.jpa.repository.JpaRepository

interface ActiveArtifactPointerRepository : JpaRepository<ActiveArtifactPointerEntity, ActiveArtifactPointerKey> {
    fun findByIdChatId(chatId: String): List<ActiveArtifactPointerEntity>

    fun findByIdChatIdAndIdPointerKeyOrderByUpdatedAtAsc(
        chatId: String,
        pointerKey: String,
    ): List<ActiveArtifactPointerEntity>

    fun deleteByIdChatIdAndIdPointerKey(chatId: String, pointerKey: String)
}
