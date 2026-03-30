package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntity

/** Entity CRUD (SPEC §7.1). */
interface MetadataEntityService {
    fun findById(id: String): MetadataEntity?
    fun findAll(): List<MetadataEntity>
    fun findByKind(kind: String): List<MetadataEntity>
    fun create(entity: MetadataEntity, actor: String): MetadataEntity
    fun update(entity: MetadataEntity, actor: String): MetadataEntity
    fun delete(id: String, actor: String)
}
