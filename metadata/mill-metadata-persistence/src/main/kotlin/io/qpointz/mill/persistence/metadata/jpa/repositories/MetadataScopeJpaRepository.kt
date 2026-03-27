package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataScopeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/**
 * Spring Data JPA repository for [MetadataScopeEntity].
 *
 * Provides CRUD operations on `metadata_scope`. The global scope is seeded by Flyway V4.
 * Other scopes are created on demand during import or facet write operations.
 */
interface MetadataScopeJpaRepository : JpaRepository<MetadataScopeEntity, Long> {

    /**
     * Finds a scope by its full Mill URN (`scope_res`).
     *
     * @param scopeRes full scope URN
     * @return the matching scope entity, or empty if not found
     */
    fun findByScopeRes(scopeRes: String): Optional<MetadataScopeEntity>

    /**
     * Finds a scope by its type and reference identifier.
     *
     * @param scopeType   coarse scope category (`USER`, `TEAM`, `ROLE`, `GLOBAL`)
     * @param referenceId the local reference identifier (user ID, team name, etc.)
     * @return the matching scope entity, or empty if not found
     */
    fun findByScopeTypeAndReferenceId(scopeType: String, referenceId: String): Optional<MetadataScopeEntity>
}
