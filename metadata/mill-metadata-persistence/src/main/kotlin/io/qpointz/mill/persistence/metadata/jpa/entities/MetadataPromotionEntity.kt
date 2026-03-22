package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

/**
 * JPA entity representing a pending or completed facet promotion request in
 * `metadata_promotion`.
 *
 * A promotion request moves a facet from a source scope (e.g. a user's personal scope) to a
 * target scope (e.g. the global scope) after review. The workflow is defined in WI-091.
 *
 * @property promotionId  stable identifier for this promotion request; typically a UUID
 * @property entityId     foreign key to the entity whose facet is being promoted
 * @property facetType    full Mill facet-type URN of the promoted facet
 * @property sourceScopeId URN of the scope being promoted from (source)
 * @property targetScopeId URN of the scope being promoted to (target)
 * @property status        workflow status: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`
 * @property requestedBy  actor who submitted the promotion request
 * @property reviewedBy   actor who reviewed (approved or rejected) the request; `null` while pending
 * @property requestedAt  timestamp when the request was submitted
 * @property reviewedAt   timestamp when the request was reviewed; `null` while pending
 * @property notes        optional reviewer notes or rejection reason
 */
@Entity
@Table(
    name = "metadata_promotion",
    indexes = [
        Index(name = "idx_mp_entity", columnList = "entity_id, facet_type"),
        Index(name = "idx_mp_status", columnList = "status")
    ]
)
class MetadataPromotionEntity(

    @Id
    @Column(name = "promotion_id", nullable = false, length = 255)
    val promotionId: String,

    @Column(name = "entity_id", nullable = false, length = 255)
    var entityId: String,

    @Column(name = "facet_type", nullable = false, length = 255)
    var facetType: String,

    @Column(name = "source_scope_id", nullable = false, length = 255)
    var sourceScopeId: String,

    @Column(name = "target_scope_id", nullable = false, length = 255)
    var targetScopeId: String,

    @Column(name = "status", nullable = false, length = 32)
    var status: String = "PENDING",

    @Column(name = "requested_by", nullable = false, length = 255)
    var requestedBy: String,

    @Column(name = "reviewed_by", length = 255)
    var reviewedBy: String?,

    @Column(name = "requested_at", nullable = false)
    val requestedAt: Instant,

    @Column(name = "reviewed_at")
    var reviewedAt: Instant?,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String?
)
