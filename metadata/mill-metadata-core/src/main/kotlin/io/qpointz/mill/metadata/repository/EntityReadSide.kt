package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntity

/**
 * Read-only projection of entity persistence (`metadata_entity`) — SPEC §6.1.
 *
 * Callers that only list or resolve entities should depend on this interface rather than
 * [EntityRepository] to avoid accidental writes.
 */
interface EntityReadSide {

    /** @param id canonical entity URN */
    fun findById(id: String): MetadataEntity?

    fun findAll(): List<MetadataEntity>

    /** @param kind opaque [MetadataEntity.kind] filter */
    fun findByKind(kind: String): List<MetadataEntity>

    /** @param id canonical entity URN */
    fun exists(id: String): Boolean
}
