package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeInstEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MetadataFacetTypeInstJpaRepository : JpaRepository<MetadataFacetTypeInstEntity, Long> {
    /**
     * Finds a runtime facet type row by its URN (`type_res`).
     *
     * @param typeRes full Mill facet-type URN
     */
    fun findByTypeRes(typeRes: String): Optional<MetadataFacetTypeInstEntity>
}
