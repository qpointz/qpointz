package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.ai.jpa.entities.ChatMemoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMemoryRepository : JpaRepository<ChatMemoryEntity, String>
