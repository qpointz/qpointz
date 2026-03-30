package io.qpointz.mill.persistence.metadata.jpa.entities

import io.qpointz.mill.persistence.metadata.jpa.listeners.MetadataFacetTypeDefAuditListener
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * JPA row for `metadata_facet_type_def`.
 *
 * @property defId surrogate PK (`def_id`)
 */
@Entity
@Table(
    name = "metadata_facet_type_def",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_metadata_facet_type_def_res", columnNames = ["type_res"]),
        UniqueConstraint(name = "uq_metadata_facet_type_def_uuid", columnNames = ["uuid"])
    ]
)
@EntityListeners(MetadataFacetTypeDefAuditListener::class)
class MetadataFacetTypeDefEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "def_id", nullable = false)
    val defId: Long = 0,

    @Column(name = "uuid", nullable = false, length = 36)
    var uuid: String,

    @Column(name = "type_res", nullable = false, length = 512)
    var typeRes: String,

    @Column(name = "manifest_json", nullable = false, columnDefinition = "TEXT")
    var manifestJson: String,

    @Column(name = "mandatory", nullable = false)
    var mandatory: Boolean = false,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant,

    @Column(name = "last_modified_by", length = 255)
    var lastModifiedBy: String?
)
