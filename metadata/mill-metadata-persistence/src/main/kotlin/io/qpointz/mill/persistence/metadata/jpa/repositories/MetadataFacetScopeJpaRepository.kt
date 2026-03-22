package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetScopeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/**
 * Spring Data JPA repository for [MetadataFacetScopeEntity].
 *
 * Provides access to `metadata_facet_scope` rows, supporting entity-scoped and
 * triple-keyed lookups.
 */
interface MetadataFacetScopeJpaRepository : JpaRepository<MetadataFacetScopeEntity, Long> {

    /**
     * Finds all facet scope rows for the given entity.
     *
     * @param entityId the entity identifier
     * @return all facet scope rows associated with the entity
     */
    fun findByEntityId(entityId: String): List<MetadataFacetScopeEntity>

    /**
     * Finds the facet scope row for a specific (entity, facet-type, scope) triple.
     *
     * @param entityId  the entity identifier
     * @param facetType full Mill facet-type URN
     * @param scopeId   full Mill scope URN (scope primary key)
     * @return the matching row, or empty if not present
     */
    fun findByEntityIdAndFacetTypeAndScopeScopeId(
        entityId: String,
        facetType: String,
        scopeId: String
    ): Optional<MetadataFacetScopeEntity>
}
