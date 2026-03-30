package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import java.time.Instant
import java.util.UUID

/**
 * @param entityRepository entity persistence
 * @param facetRepository used to cascade-delete facet rows when an entity is removed
 */
class DefaultMetadataEntityService(
    private val entityRepository: MetadataEntityRepository,
    private val facetRepository: FacetRepository
) : MetadataEntityService {

    override fun findById(id: String): MetadataEntity? =
        entityRepository.findById(MetadataEntityUrn.canonicalize(id))

    override fun findAll(): List<MetadataEntity> = entityRepository.findAll()

    override fun findByKind(kind: String): List<MetadataEntity> = entityRepository.findByKind(kind)

    override fun create(entity: MetadataEntity, actor: String): MetadataEntity {
        val cid = MetadataEntityUrn.canonicalize(entity.id)
        require(MetadataEntityUrn.isMillUrn(cid)) { "Entity id must be a Mill URN: $cid" }
        require(!entityRepository.exists(cid)) { "Entity already exists: $cid" }
        val now = Instant.now()
        val uuid = entity.uuid ?: UUID.randomUUID().toString()
        val row = entity.copy(
            id = cid,
            uuid = uuid,
            createdAt = now,
            createdBy = actor,
            lastModifiedAt = now,
            lastModifiedBy = actor
        )
        return entityRepository.save(row)
    }

    override fun update(entity: MetadataEntity, actor: String): MetadataEntity {
        val cid = MetadataEntityUrn.canonicalize(entity.id)
        val existing = entityRepository.findById(cid)
            ?: throw IllegalArgumentException("Unknown entity: $cid")
        val now = Instant.now()
        return entityRepository.save(
            entity.copy(
                id = cid,
                uuid = existing.uuid,
                createdAt = existing.createdAt,
                createdBy = existing.createdBy,
                lastModifiedAt = now,
                lastModifiedBy = actor
            )
        )
    }

    override fun delete(id: String, actor: String) {
        val cid = MetadataEntityUrn.canonicalize(id)
        facetRepository.deleteByEntity(cid)
        entityRepository.delete(cid)
    }
}
