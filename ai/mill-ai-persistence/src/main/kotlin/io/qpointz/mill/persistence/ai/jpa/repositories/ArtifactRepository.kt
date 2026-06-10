package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ArtifactEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ArtifactRepository : JpaRepository<ArtifactEntity, String> {
    fun findByConversationIdOrderByCreatedAtAsc(conversationId: String): List<ArtifactEntity>
    fun findByRunIdOrderByCreatedAtAsc(runId: String): List<ArtifactEntity>
}
