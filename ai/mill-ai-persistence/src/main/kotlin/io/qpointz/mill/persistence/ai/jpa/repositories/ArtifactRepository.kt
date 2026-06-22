package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ArtifactEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ArtifactRepository : JpaRepository<ArtifactEntity, String> {
    fun findByChatIdOrderByCreatedAtAsc(chatId: String): List<ArtifactEntity>

    fun findByRunIdOrderByCreatedAtAsc(runId: String): List<ArtifactEntity>

    /** Artifacts persisted before the owning turn row exists (capture path). */
    fun findByChatIdAndTurnId(chatId: String, turnId: String): List<ArtifactEntity>
}
