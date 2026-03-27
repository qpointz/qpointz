package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataFacetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface MetadataFacetJpaRepository : JpaRepository<MetadataFacetEntity, Long> {

    @Query(
        """
        SELECT f FROM MetadataFacetEntity f
        JOIN f.entity e
        WHERE e.entityRes = :entityRes
        """
    )
    fun findByEntityEntityRes(@Param("entityRes") entityRes: String): List<MetadataFacetEntity>

    fun findByFacetUid(facetUid: String): Optional<MetadataFacetEntity>

    @Query(
        """
        SELECT COUNT(f) FROM MetadataFacetEntity f
        JOIN f.entity e
        JOIN f.facetType t
        JOIN f.scope s
        WHERE e.entityRes = :entityRes
          AND t.typeRes = :facetTypeRes
          AND s.scopeRes = :scopeRes
        """
    )
    fun countByEntityResAndFacetTypeResAndScopeRes(
        @Param("entityRes") entityRes: String,
        @Param("facetTypeRes") facetTypeRes: String,
        @Param("scopeRes") scopeRes: String
    ): Long

    @Query(
        """
        SELECT f FROM MetadataFacetEntity f
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
    ): List<MetadataFacetEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        DELETE FROM MetadataFacetEntity f
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

    /**
     * Removes all facet payload rows for an entity. Used before re-inserting from domain so removals
     * of entire facet types or scopes persist (the save loop only touches keys still present on the entity).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MetadataFacetEntity f WHERE f.entity.entityRes = :entityRes")
    fun deleteByEntityEntityRes(@Param("entityRes") entityRes: String)

    @Query("SELECT COUNT(f) FROM MetadataFacetEntity f WHERE f.facetType.typeRes = :typeRes")
    fun countByFacetTypeTypeRes(@Param("typeRes") typeRes: String): Long
}
