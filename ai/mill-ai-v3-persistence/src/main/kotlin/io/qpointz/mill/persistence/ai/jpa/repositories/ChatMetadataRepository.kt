package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ChatMetadataEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMetadataRepository : JpaRepository<ChatMetadataEntity, String> {
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<ChatMetadataEntity>
    fun findByUserIdAndContextTypeAndContextId(userId: String, contextType: String, contextId: String): ChatMetadataEntity?
}
