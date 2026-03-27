package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import java.util.Optional

/** Persistence abstraction for [MetadataEntity] documents. */
interface MetadataRepository {
    /** Saves (inserts or updates) the given entity. */
    fun save(entity: MetadataEntity)

    /** Returns the entity with the given identifier, or empty if not found. */
    fun findById(id: String): Optional<MetadataEntity>

    /** Returns the entity at the given schema/table/attribute coordinates, or empty if not found. */
    fun findByLocation(schema: String?, table: String?, attribute: String?): Optional<MetadataEntity>

    /** Returns all entities of the given [MetadataType]. */
    fun findByType(type: MetadataType): List<MetadataEntity>

    /** Returns all entities in the repository. */
    fun findAll(): List<MetadataEntity>

    /** Deletes the entity with the given identifier; no-op if absent. */
    fun deleteById(id: String)

    /** Returns `true` if an entity with the given identifier exists. */
    fun existsById(id: String): Boolean

    /**
     * Deletes all entities in the repository.
     *
     * Used by [io.qpointz.mill.metadata.service.MetadataImportService] when operating in
     * [io.qpointz.mill.metadata.service.ImportMode.REPLACE] mode.
     */
    fun deleteAll()

    /**
     * All facet instance rows for an entity (JPA). File-backed repositories return an empty list.
     */
    fun listFacetInstanceRows(entityRes: String): List<MetadataFacetInstanceRow> = emptyList()

    /**
     * Looks up a facet row by [facetUid], verifying it belongs to [entityRes].
     */
    fun findFacetInstanceRow(entityRes: String, facetUid: String): MetadataFacetInstanceRow? = null

    /**
     * Deletes the facet row with the given [facetUid] for [entityRes]. Returns whether a row was removed.
     */
    fun deleteFacetRowByUid(entityRes: String, facetUid: String): Boolean = false

    /**
     * Number of persisted facet rows for the entity / facet type / scope triple.
     */
    fun countFacetInstancesAtScope(entityRes: String, facetTypeUrn: String, scopeUrn: String): Int = 0

    /**
     * Declared target cardinality for a facet type (defaults to [FacetTargetCardinality.SINGLE]).
     */
    fun resolveFacetTargetCardinality(facetTypeUrn: String): FacetTargetCardinality =
        FacetTargetCardinality.SINGLE
}
