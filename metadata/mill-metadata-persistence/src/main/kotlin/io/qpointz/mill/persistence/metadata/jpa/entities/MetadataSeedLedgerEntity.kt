package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/** JPA row for `metadata_seed` startup ledger. */
@Entity
@Table(
    name = "metadata_seed",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_metadata_seed_key", columnNames = ["seed_key"]),
        UniqueConstraint(name = "uq_metadata_seed_uuid", columnNames = ["uuid"])
    ]
)
class MetadataSeedLedgerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id", nullable = false)
    val ledgerId: Long = 0,

    @Column(name = "uuid", nullable = false, length = 36)
    var uuid: String,

    @Column(name = "seed_key", nullable = false, length = 512)
    var seedKey: String,

    @Column(name = "completed_at")
    var completedAt: Instant?,

    @Column(name = "fingerprint", length = 512)
    var fingerprint: String?,

    @Column(name = "last_error", columnDefinition = "TEXT")
    var lastError: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant,

    @Column(name = "last_modified_by", length = 255)
    var lastModifiedBy: String?
)
