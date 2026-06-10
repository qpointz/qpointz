package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.AiValueMappingStateEntity
import org.springframework.data.jpa.repository.JpaRepository

/** Spring Data repository for [AiValueMappingStateEntity] (`ai_value_mapping_state`). */
interface AiValueMappingStateJpaRepository : JpaRepository<AiValueMappingStateEntity, String>
