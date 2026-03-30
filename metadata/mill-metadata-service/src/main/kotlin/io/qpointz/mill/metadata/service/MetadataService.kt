package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntity
import java.util.Optional

/** Read-side metadata entity API used by REST controllers and service-layer facades. */
interface MetadataService {

    /** @param id entity URN or legacy catalog key accepted by [io.qpointz.mill.metadata.domain.MetadataEntityUrn] rules */
    fun findById(id: String): Optional<MetadataEntity>

    /** @return all persisted entity identity rows */
    fun findAll(): List<MetadataEntity>

    /** @param kind opaque persisted [MetadataEntity.kind] (e.g. schema, table, attribute); not inferred by metadata */
    fun findByKind(kind: String): List<MetadataEntity>
}
