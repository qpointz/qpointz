package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.repository.MetadataContentRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataContentEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataContentJpaRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * JPA adapter for {@link MetadataContentRepository}.
 *
 * @param jpa Spring Data repository for {@code metadata_content}
 */
@Transactional
class JpaMetadataContentRepository(
    private val jpa: MetadataContentJpaRepository,
) : MetadataContentRepository {

    override fun findByContentUrn(contentUrn: String): MetadataContent? {
        val urn = MetadataEntityUrn.canonicalize(contentUrn)
        return jpa.findByContentUrn(urn).map { toDomain(it) }.orElse(null)
    }

    override fun findByTarget(targetUrn: String, contentKind: String?): List<MetadataContent> {
        val target = MetadataEntityUrn.canonicalize(targetUrn)
        val rows = if (contentKind == null) {
            jpa.findByTargetUrnOrderBySortOrderAscContentUrnAsc(target)
        } else {
            jpa.findByTargetUrnAndContentKindOrderBySortOrderAscContentUrnAsc(target, contentKind)
        }
        return rows.map { toDomain(it) }
    }

    override fun findAll(): List<MetadataContent> =
        jpa.findAll().map { toDomain(it) }.sortedBy { it.contentUrn }

    override fun save(content: MetadataContent): MetadataContent {
        val urn = MetadataEntityUrn.canonicalize(content.contentUrn)
        val target = MetadataEntityUrn.canonicalize(content.targetUrn)
        val scope = content.scopeUrn?.let { MetadataEntityUrn.canonicalize(it) }
        val now = Instant.now()
        val row = jpa.findByContentUrn(urn).orElse(null)
        val saved = if (row == null) {
            jpa.save(
                MetadataContentEntity(
                    uuid = content.uuid ?: UUID.randomUUID().toString(),
                    contentUrn = urn,
                    contentKind = content.contentKind,
                    targetUrn = target,
                    scopeUrn = scope,
                    title = content.title,
                    description = content.description,
                    contentBody = content.contentBody,
                    mediaType = content.mediaType,
                    sortOrder = content.sortOrder,
                    enabled = content.enabled,
                    schemaVersion = content.schemaVersion,
                    createdAt = now,
                    createdBy = content.createdBy,
                    lastModifiedAt = now,
                    lastModifiedBy = content.lastModifiedBy,
                ),
            )
        } else {
            row.contentKind = content.contentKind
            row.targetUrn = target
            row.scopeUrn = scope
            row.title = content.title
            row.description = content.description
            row.contentBody = content.contentBody
            row.mediaType = content.mediaType
            row.sortOrder = content.sortOrder
            row.enabled = content.enabled
            row.schemaVersion = content.schemaVersion
            content.uuid?.let { row.uuid = it }
            row.lastModifiedAt = now
            row.lastModifiedBy = content.lastModifiedBy
            jpa.save(row)
        }
        return toDomain(saved)
    }

    private fun toDomain(e: MetadataContentEntity): MetadataContent = MetadataContent(
        contentUrn = e.contentUrn,
        contentKind = e.contentKind,
        targetUrn = e.targetUrn,
        scopeUrn = e.scopeUrn,
        title = e.title,
        description = e.description,
        contentBody = e.contentBody,
        mediaType = e.mediaType,
        sortOrder = e.sortOrder,
        enabled = e.enabled,
        schemaVersion = e.schemaVersion,
        uuid = e.uuid,
        createdAt = e.createdAt,
        createdBy = e.createdBy,
        lastModifiedAt = e.lastModifiedAt,
        lastModifiedBy = e.lastModifiedBy,
    )
}
