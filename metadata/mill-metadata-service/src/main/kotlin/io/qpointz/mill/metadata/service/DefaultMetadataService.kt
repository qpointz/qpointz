package io.qpointz.mill.metadata.service

import io.qpointz.mill.data.schema.MetadataEntityUrnCodec
import io.qpointz.mill.metadata.domain.MetadataEntity
import java.util.Optional

/**
 * @param entityService greenfield entity persistence
 * @param urnCodec builds canonical URNs for legacy coordinate keys
 */
class DefaultMetadataService(
    private val entityService: MetadataEntityService,
    private val urnCodec: MetadataEntityUrnCodec
) : MetadataService {

    override fun findById(id: String): Optional<MetadataEntity> {
        val cid = MetadataEntityIdResolver.resolve(id, urnCodec)
        return Optional.ofNullable(entityService.findById(cid))
    }

    override fun findAll(): List<MetadataEntity> = entityService.findAll()

    override fun findByKind(kind: String): List<MetadataEntity> = entityService.findByKind(kind)
}
