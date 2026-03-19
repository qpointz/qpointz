package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.RunEventEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RunEventRepository : JpaRepository<RunEventEntity, String> {
    fun findByRunIdOrderByCreatedAtAsc(runId: String): List<RunEventEntity>
}
