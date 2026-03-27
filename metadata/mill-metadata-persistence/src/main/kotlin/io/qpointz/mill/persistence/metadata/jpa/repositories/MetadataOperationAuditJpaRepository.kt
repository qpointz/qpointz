package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataOperationAuditEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for [MetadataOperationAuditEntity].
 *
 * Provides read access to the append-only `metadata_operation_audit` table.
 * Rows are written by [io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataChangeObserver]
 * and are never updated or deleted.
 */
interface MetadataOperationAuditJpaRepository : JpaRepository<MetadataOperationAuditEntity, String> {

    /**
     * Finds all audit entries for a specific entity business id (`entity_res`).
     *
     * @param entityRes the domain entity FQDN
     * @return all audit entries referencing the entity
     */
    fun findByEntityRes(entityRes: String): List<MetadataOperationAuditEntity>

    /**
     * Finds all audit entries recorded by a specific actor.
     *
     * @param actorId the actor identifier
     * @return all audit entries attributed to the actor
     */
    fun findByActorId(actorId: String): List<MetadataOperationAuditEntity>

    /**
     * Finds all audit entries for a specific entity and facet type combination.
     *
     * @param entityRes     the domain entity FQDN
     * @param facetTypeRes full Mill facet-type URN
     * @return all audit entries matching both entity and facet type
     */
    fun findByEntityResAndFacetTypeRes(entityRes: String, facetTypeRes: String): List<MetadataOperationAuditEntity>
}
