package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeDefEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/** Spring Data repository for [MetadataFacetTypeDefEntity] (`metadata_facet_type_def`). */
interface MetadataFacetTypeJpaRepository : JpaRepository<MetadataFacetTypeDefEntity, Long> {

    fun findByTypeRes(typeRes: String): Optional<MetadataFacetTypeDefEntity>

    fun existsByTypeRes(typeRes: String): Boolean
}
