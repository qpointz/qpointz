package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import java.util.Optional

/** Persistence abstraction for [MetadataEntity] documents. */
interface MetadataRepository {
    fun save(entity: MetadataEntity)
    fun findById(id: String): Optional<MetadataEntity>
    fun findByLocation(schema: String?, table: String?, attribute: String?): Optional<MetadataEntity>
    fun findByType(type: MetadataType): List<MetadataEntity>
    fun findAll(): List<MetadataEntity>
    fun deleteById(id: String)
    fun existsById(id: String): Boolean
}
