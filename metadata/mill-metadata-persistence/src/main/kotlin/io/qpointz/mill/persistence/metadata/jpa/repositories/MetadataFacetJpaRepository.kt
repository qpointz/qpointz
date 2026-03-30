package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityFacetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

/** Spring Data repository for [MetadataEntityFacetEntity] (`metadata_entity_facet`). */
interface MetadataFacetJpaRepository : JpaRepository<MetadataEntityFacetEntity, Long> {

    fun findByUuid(uuid: String): Optional<MetadataEntityFacetEntity>

    @Query(
        """
        SELECT f FROM MetadataEntityFacetEntity f
        JOIN f.entity e
        WHERE e.entityRes = :entityRes
        """
    )
    fun findByEntityEntityRes(@Param("entityRes") entityRes: String): List<MetadataEntityFacetEntity>

    @Query(
        """
        SELECT f FROM MetadataEntityFacetEntity f
        JOIN f.entity e
        JOIN f.facetType t
        JOIN f.scope s
        WHERE e.entityRes = :entityRes
          AND t.typeRes = :facetTypeRes
          AND s.scopeRes = :scopeRes
        ORDER BY f.facetId ASC
        """
    )
    fun listByEntityResFacetTypeResScopeRes(
        @Param("entityRes") entityRes: String,
        @Param("facetTypeRes") facetTypeRes: String,
        @Param("scopeRes") scopeRes: String
    ): List<MetadataEntityFacetEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        DELETE FROM MetadataEntityFacetEntity f
        WHERE f.entity.entityRes = :entityRes
          AND f.facetType.typeRes = :facetTypeRes
          AND f.scope.scopeRes = :scopeRes
        """
    )
    fun deleteByEntityResAndFacetTypeResAndScopeRes(
        @Param("entityRes") entityRes: String,
        @Param("facetTypeRes") facetTypeRes: String,
        @Param("scopeRes") scopeRes: String
    )

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MetadataEntityFacetEntity f WHERE f.entity.entityRes = :entityRes")
    fun deleteByEntityEntityRes(@Param("entityRes") entityRes: String)

    @Query("SELECT COUNT(f) FROM MetadataEntityFacetEntity f WHERE f.facetType.typeRes = :typeRes")
    fun countByFacetTypeTypeRes(@Param("typeRes") typeRes: String): Long
}
