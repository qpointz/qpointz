package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntity

/** Persistence for [MetadataEntity] rows (`metadata_entity`) — SPEC §6.1. */
interface MetadataEntityRepository {
    /** @param id canonical entity URN */
    fun findById(id: String): MetadataEntity?

    fun findAll(): List<MetadataEntity>

    /** @param kind opaque [MetadataEntity.kind] filter */
    fun findByKind(kind: String): List<MetadataEntity>

    /** @param id canonical entity URN */
    fun exists(id: String): Boolean

    fun save(entity: MetadataEntity): MetadataEntity

    /** @param id canonical entity URN */
    fun delete(id: String)

    fun deleteAll()
}
