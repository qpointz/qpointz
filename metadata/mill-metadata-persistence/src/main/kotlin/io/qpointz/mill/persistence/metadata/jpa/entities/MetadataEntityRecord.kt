package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import io.qpointz.mill.persistence.metadata.jpa.listeners.MetadataEntityRecordAuditListener
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * JPA row for `metadata_entity` (greenfield DDL).
 *
 * @property entityId surrogate PK
 * @property uuid stable external id (matches domain entity uuid after first save)
 * @property entityRes canonical entity URN
 * @property entityKind optional opaque kind label
 */
@Entity
@Table(
    name = "metadata_entity",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_metadata_entity_res", columnNames = ["entity_res"]),
        UniqueConstraint(name = "uq_metadata_entity_uuid", columnNames = ["uuid"])
    ]
)
@EntityListeners(MetadataEntityRecordAuditListener::class)
class MetadataEntityRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entity_id", nullable = false)
    val entityId: Long = 0,

    @Column(name = "uuid", nullable = false, length = 36)
    var uuid: String,

    @Column(name = "entity_res", nullable = false, length = 512)
    var entityRes: String,

    @Column(name = "entity_kind", length = 255)
    var entityKind: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant,

    @Column(name = "last_modified_by", length = 255)
    var lastModifiedBy: String?
)
