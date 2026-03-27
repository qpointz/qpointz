package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataPromotionEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for [MetadataPromotionEntity].
 *
 * Provides CRUD operations on `metadata_promotion`. The promotion workflow is defined
 * in WI-091; only the persistence layer is wired here.
 */
interface MetadataPromotionJpaRepository : JpaRepository<MetadataPromotionEntity, String> {

    /**
     * Finds all promotion requests for a specific entity and facet type.
     *
     * @param entityRes     domain entity FQDN (`entity_res`)
     * @param facetTypeRes full Mill facet-type URN
     * @return all matching promotion request rows
     */
    fun findByEntityResAndFacetTypeRes(entityRes: String, facetTypeRes: String): List<MetadataPromotionEntity>

    /**
     * Finds all promotion requests with the given workflow status.
     *
     * @param status workflow status string (e.g. `PENDING`, `APPROVED`, `REJECTED`)
     * @return all matching promotion request rows
     */
    fun findByStatus(status: String): List<MetadataPromotionEntity>
}
