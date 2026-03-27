package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/**
 * Spring Data JPA repository for [MetadataFacetTypeEntity] (`metadata_facet_type_def`).
 */
interface MetadataFacetTypeJpaRepository : JpaRepository<MetadataFacetTypeEntity, Long> {

    /**
     * Finds a facet type definition by its Mill URN (`type_res`).
     *
     * @param typeRes full facet-type URN
     * @return the row, or empty if not found
     */
    fun findByTypeRes(typeRes: String): Optional<MetadataFacetTypeEntity>

    fun existsByTypeRes(typeRes: String): Boolean
}
