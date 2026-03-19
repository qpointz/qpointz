package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ConversationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConversationRepository : JpaRepository<ConversationEntity, String>
