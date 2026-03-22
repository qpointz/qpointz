package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetTypeEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for [MetadataFacetTypeEntity].
 *
 * Provides standard CRUD operations on `metadata_facet_type`.
 * The five platform facet types are seeded by Flyway V4 and are always present.
 */
interface MetadataFacetTypeJpaRepository : JpaRepository<MetadataFacetTypeEntity, String>
