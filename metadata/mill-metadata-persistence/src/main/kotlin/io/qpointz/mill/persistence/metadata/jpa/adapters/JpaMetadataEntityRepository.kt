package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * @param jpa Spring Data repository for `metadata_entity`
 */
@Transactional
class JpaMetadataEntityRepository(
    private val jpa: MetadataEntityJpaRepository
) : MetadataEntityRepository {

    override fun findById(id: String): MetadataEntity? {
        val res = MetadataEntityUrn.canonicalize(id)
        return jpa.findByEntityRes(res).map { toDomain(it) }.orElse(null)
    }

    override fun findAll(): List<MetadataEntity> = jpa.findAll().map { toDomain(it) }

    override fun findByKind(kind: String): List<MetadataEntity> =
        jpa.findByEntityKind(kind.lowercase()).map { toDomain(it) }

    override fun exists(id: String): Boolean =
        jpa.existsByEntityRes(MetadataEntityUrn.canonicalize(id))

    override fun save(entity: MetadataEntity): MetadataEntity {
        val res = MetadataEntityUrn.canonicalize(entity.id)
        val now = Instant.now()
        val row = jpa.findByEntityRes(res).orElse(null)
        val persisted = if (row == null) {
            jpa.save(
                MetadataEntityRecord(
                    uuid = entity.uuid ?: UUID.randomUUID().toString(),
                    entityRes = res,
                    entityKind = entity.kind?.lowercase(),
                    createdAt = now,
                    createdBy = entity.createdBy,
                    lastModifiedAt = now,
                    lastModifiedBy = entity.lastModifiedBy
                )
            )
        } else {
            row.entityKind = entity.kind?.lowercase()
            row.lastModifiedAt = now
            row.lastModifiedBy = entity.lastModifiedBy
            entity.uuid?.let { row.uuid = it }
            jpa.save(row)
        }
        return toDomain(persisted)
    }

    override fun delete(id: String) {
        jpa.deleteByEntityRes(MetadataEntityUrn.canonicalize(id))
    }

    override fun deleteAll() {
        jpa.deleteAll()
    }

    private fun toDomain(e: MetadataEntityRecord): MetadataEntity = MetadataEntity(
        id = e.entityRes,
        kind = e.entityKind,
        uuid = e.uuid,
        createdAt = e.createdAt,
        createdBy = e.createdBy,
        lastModifiedAt = e.lastModifiedAt,
        lastModifiedBy = e.lastModifiedBy
    )
}
