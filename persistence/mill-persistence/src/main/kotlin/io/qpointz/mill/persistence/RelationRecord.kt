package io.qpointz.mill.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * Generic cross-model relation record.
 *
 * Owned by `mill-persistence`; domain-specific relation semantics and URN
 * construction belong in domain persistence modules.
 */
@Entity
@Table(name = "relation_record")
class RelationRecord(
    @Id
    @Column(name = "relation_id", nullable = false, length = 255)
    val relationId: String,

    @Column(name = "relation_kind", nullable = false, length = 255)
    val relationKind: String,

    @Column(name = "source_id", nullable = false, length = 255)
    val sourceId: String,

    @Column(name = "source_type", nullable = false, length = 255)
    val sourceType: String,

    @Column(name = "source_urn", nullable = false, length = 1024)
    val sourceUrn: String,

    @Column(name = "target_id", nullable = false, length = 255)
    val targetId: String,

    @Column(name = "target_type", nullable = false, length = 255)
    val targetType: String,

    @Column(name = "target_urn", nullable = false, length = 1024)
    val targetUrn: String,

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    val metadataJson: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
