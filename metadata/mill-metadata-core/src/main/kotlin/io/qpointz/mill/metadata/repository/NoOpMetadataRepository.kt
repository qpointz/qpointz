package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import java.util.Optional

/**
 * No-operation [MetadataRepository] that simulates an absent metadata store.
 *
 * All read operations return empty results; all write and delete operations are silent no-ops.
 * Used as the autoconfiguration fallback when no storage backend
 * (`mill.metadata.storage.type`) is configured, so that dependent services start cleanly
 * without requiring an explicit persistence setup.
 */
object NoOpMetadataRepository : MetadataRepository {

    /** No-op: entities are not persisted. */
    override fun save(entity: MetadataEntity) = Unit

    /** Always returns [Optional.empty]: no entities are stored. */
    override fun findById(id: String): Optional<MetadataEntity> = Optional.empty()

    /** Always returns [Optional.empty]: no entities are stored. */
    override fun findByLocation(schema: String?, table: String?, attribute: String?): Optional<MetadataEntity> =
        Optional.empty()

    /** Always returns an empty list: no entities are stored. */
    override fun findByType(type: MetadataType): List<MetadataEntity> = emptyList()

    /** Always returns an empty list: no entities are stored. */
    override fun findAll(): List<MetadataEntity> = emptyList()

    /** No-op: nothing to delete. */
    override fun deleteById(id: String) = Unit

    /** Always returns `false`: no entities are stored. */
    override fun existsById(id: String): Boolean = false

    /** No-op: nothing to delete. */
    override fun deleteAll() = Unit
}
