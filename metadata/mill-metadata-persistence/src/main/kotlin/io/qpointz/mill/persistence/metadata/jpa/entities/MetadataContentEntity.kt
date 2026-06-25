package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * JPA row for {@code metadata_content} authoring attachments.
 */
@Entity
@Table(
    name = "metadata_content",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_metadata_content_urn", columnNames = ["content_urn"]),
        UniqueConstraint(name = "uq_metadata_content_uuid", columnNames = ["uuid"]),
    ],
)
class MetadataContentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id", nullable = false)
    val contentId: Long = 0,

    @Column(name = "uuid", nullable = false, length = 36)
    var uuid: String,

    @Column(name = "content_urn", nullable = false, length = 512)
    var contentUrn: String,

    @Column(name = "content_kind", nullable = false, length = 64)
    var contentKind: String,

    @Column(name = "target_urn", nullable = false, length = 512)
    var targetUrn: String,

    @Column(name = "scope_urn", length = 512)
    var scopeUrn: String?,

    @Column(name = "title", length = 512)
    var title: String?,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String?,

    @Column(name = "content_body", nullable = false, columnDefinition = "TEXT")
    var contentBody: String,

    @Column(name = "media_type", nullable = false, length = 128)
    var mediaType: String = "application/json",

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Column(name = "schema_version", length = 32)
    var schemaVersion: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant,

    @Column(name = "last_modified_by", length = 255)
    var lastModifiedBy: String?,
)
