package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntity

/** In-memory no-op / empty [MetadataEntityRepository] for tests and disabled metadata. */
class NoOpMetadataEntityRepository : MetadataEntityRepository {
    override fun findById(id: String): MetadataEntity? = null
    override fun findAll(): List<MetadataEntity> = emptyList()
    override fun findByKind(kind: String): List<MetadataEntity> = emptyList()
    override fun exists(id: String): Boolean = false
    override fun save(entity: MetadataEntity): MetadataEntity = entity
    override fun delete(id: String) = Unit
    override fun deleteAll() = Unit
}
