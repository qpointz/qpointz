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
interface MetadataScopeJpaRepository : JpaRepository<MetadataScopeEntity, String> {

    /**
     * Finds a scope by its type and reference identifier.
     *
     * Used by [io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataRepository] to
     * look up or create scopes without knowing their primary key (full URN) in advance.
     *
     * @param scopeType   coarse scope category (`USER`, `TEAM`, `ROLE`, `GLOBAL`)
     * @param referenceId the local reference identifier (user ID, team name, etc.)
     * @return the matching scope entity, or empty if not found
     */
    fun findByScopeTypeAndReferenceId(scopeType: String, referenceId: String): Optional<MetadataScopeEntity>
}
