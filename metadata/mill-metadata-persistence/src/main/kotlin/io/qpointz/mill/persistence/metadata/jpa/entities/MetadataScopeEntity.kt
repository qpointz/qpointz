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
 * JPA row for `metadata_scope`.
 */
@Entity
@Table(
    name = "metadata_scope",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_metadata_scope_res", columnNames = ["scope_res"]),
        UniqueConstraint(name = "uq_metadata_scope_uuid", columnNames = ["uuid"])
    ]
)
class MetadataScopeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scope_id", nullable = false)
    val scopeId: Long = 0,

    @Column(name = "uuid", nullable = false, length = 36)
    var uuid: String,

    @Column(name = "scope_res", nullable = false, length = 512)
    var scopeRes: String,

    @Column(name = "scope_type", nullable = false, length = 32)
    var scopeType: String,

    @Column(name = "reference_id", length = 255)
    var referenceId: String?,

    @Column(name = "display_name", length = 512)
    var displayName: String?,

    @Column(name = "owner_id", length = 255)
    var ownerId: String?,

    @Column(name = "visibility", nullable = false, length = 32)
    var visibility: String = "PUBLIC",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant,

    @Column(name = "last_modified_by", length = 255)
    var lastModifiedBy: String?
)
