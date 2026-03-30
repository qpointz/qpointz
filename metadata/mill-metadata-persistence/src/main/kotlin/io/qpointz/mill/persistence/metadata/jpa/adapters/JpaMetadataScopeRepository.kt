package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataScopeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * @param jpa Spring Data repository for `metadata_scope`
 */
@Transactional
class JpaMetadataScopeRepository(
    private val jpa: MetadataScopeJpaRepository
) : MetadataScopeRepository {

    override fun findByRes(scopeRes: String): MetadataScope? {
        val res = MetadataEntityUrn.canonicalize(scopeRes)
        return jpa.findByScopeRes(res).map { toDomain(it) }.orElse(null)
    }

    override fun findAll(): List<MetadataScope> = jpa.findAll().map { toDomain(it) }

    override fun findByType(scopeType: String): List<MetadataScope> =
        jpa.findByScopeType(scopeType.uppercase()).map { toDomain(it) }

    override fun exists(scopeRes: String): Boolean =
        jpa.existsByScopeRes(MetadataEntityUrn.canonicalize(scopeRes))

    override fun save(scope: MetadataScope): MetadataScope {
        val res = MetadataEntityUrn.canonicalize(scope.res)
        val now = Instant.now()
        val row = jpa.findByScopeRes(res).orElse(null)
        val saved = if (row == null) {
            jpa.save(
                MetadataScopeEntity(
                    uuid = scope.uuid ?: UUID.randomUUID().toString(),
                    scopeRes = res,
                    scopeType = scope.scopeType.uppercase(),
                    referenceId = scope.referenceId,
                    displayName = scope.displayName,
                    ownerId = scope.ownerId,
                    visibility = scope.visibility.uppercase(),
                    createdAt = now,
                    createdBy = scope.createdBy,
                    lastModifiedAt = now,
                    lastModifiedBy = scope.lastModifiedBy
                )
            )
        } else {
            row.scopeType = scope.scopeType.uppercase()
            row.referenceId = scope.referenceId
            row.displayName = scope.displayName
            row.ownerId = scope.ownerId
            row.visibility = scope.visibility.uppercase()
            scope.uuid?.let { row.uuid = it }
            row.lastModifiedAt = now
            row.lastModifiedBy = scope.lastModifiedBy
            jpa.save(row)
        }
        return toDomain(saved)
    }

    override fun delete(scopeRes: String) {
        jpa.deleteByScopeRes(MetadataEntityUrn.canonicalize(scopeRes))
    }

    private fun toDomain(e: MetadataScopeEntity): MetadataScope = MetadataScope(
        res = e.scopeRes,
        scopeType = e.scopeType,
        referenceId = e.referenceId,
        displayName = e.displayName,
        ownerId = e.ownerId,
        visibility = e.visibility,
        uuid = e.uuid,
        createdAt = e.createdAt,
        createdBy = e.createdBy,
        lastModifiedAt = e.lastModifiedAt,
        lastModifiedBy = e.lastModifiedBy
    )
}
