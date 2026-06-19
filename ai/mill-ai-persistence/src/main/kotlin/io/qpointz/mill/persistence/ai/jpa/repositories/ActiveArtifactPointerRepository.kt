package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ActiveArtifactPointerEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ActiveArtifactPointerRepository : JpaRepository<ActiveArtifactPointerEntity, io.qpointz.mill.persistence.ai.jpa.entities.ActiveArtifactPointerKey> {
    fun findByIdChatId(chatId: String): List<ActiveArtifactPointerEntity>
}
