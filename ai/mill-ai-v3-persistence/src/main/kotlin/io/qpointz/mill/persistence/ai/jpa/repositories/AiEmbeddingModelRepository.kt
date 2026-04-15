package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.AiEmbeddingModelEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AiEmbeddingModelRepository : JpaRepository<AiEmbeddingModelEntity, Long> {
    fun findByConfigFingerprint(configFingerprint: String): AiEmbeddingModelEntity?
}
