package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.AiValueMappingEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AiValueMappingRepository : JpaRepository<AiValueMappingEntity, UUID> {
    fun findAllByAttributeUrn(attributeUrn: String): List<AiValueMappingEntity>

    fun findByAttributeUrnAndContent(attributeUrn: String, content: String): AiValueMappingEntity?
}
