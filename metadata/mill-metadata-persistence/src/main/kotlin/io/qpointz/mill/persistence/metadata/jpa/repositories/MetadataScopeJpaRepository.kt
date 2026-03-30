package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataScopeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/** Spring Data repository for [MetadataScopeEntity] (`metadata_scope`). */
interface MetadataScopeJpaRepository : JpaRepository<MetadataScopeEntity, Long> {

    fun findByScopeRes(scopeRes: String): Optional<MetadataScopeEntity>

    fun existsByScopeRes(scopeRes: String): Boolean

    fun findByScopeType(scopeType: String): List<MetadataScopeEntity>

    fun deleteByScopeRes(scopeRes: String)
}
