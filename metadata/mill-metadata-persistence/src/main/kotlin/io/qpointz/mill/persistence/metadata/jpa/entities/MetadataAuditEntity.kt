package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/** JPA row for append-only `metadata_audit`. */
@Entity
@Table(name = "metadata_audit")
class MetadataAuditEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id", nullable = false)
    val auditId: Long = 0,

    @Column(name = "operation", nullable = false, length = 64)
    var operation: String,

    @Column(name = "subject_type", nullable = false, length = 32)
    var subjectType: String,

    @Column(name = "subject_ref", length = 512)
    var subjectRef: String?,

    @Column(name = "actor_id", length = 255)
    var actorId: String?,

    @Column(name = "correlation_id", length = 255)
    var correlationId: String?,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant = Instant.now(),

    @Column(name = "payload_before", columnDefinition = "TEXT")
    var payloadBefore: String?,

    @Column(name = "payload_after", columnDefinition = "TEXT")
    var payloadAfter: String?
)
