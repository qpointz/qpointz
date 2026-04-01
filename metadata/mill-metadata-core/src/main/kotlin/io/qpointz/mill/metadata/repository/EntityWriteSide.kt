package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntity

/**
 * Write projection of entity persistence (`metadata_entity`) — SPEC §6.1.
 *
 * Orchestration ([io.qpointz.mill.metadata.service.MetadataEntityService]) should own validation;
 * this interface is raw persistence only.
 */
interface EntityWriteSide {

    fun save(entity: MetadataEntity): MetadataEntity

    /** @param id canonical entity URN */
    fun delete(id: String)

    fun deleteAll()
}
