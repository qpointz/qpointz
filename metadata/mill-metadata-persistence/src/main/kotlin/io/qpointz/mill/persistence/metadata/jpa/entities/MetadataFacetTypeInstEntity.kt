package io.qpointz.mill.persistence.metadata.jpa.entities

import io.qpointz.mill.persistence.metadata.jpa.listeners.MetadataFacetTypeInstAuditListener
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * JPA row for runtime `metadata_facet_type`.
 */
@Entity
@Table(
    name = "metadata_facet_type",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_metadata_facet_type_res", columnNames = ["type_res"]),
        UniqueConstraint(name = "uq_metadata_facet_type_uuid", columnNames = ["uuid"])
    ]
)
@EntityListeners(MetadataFacetTypeInstAuditListener::class)
class MetadataFacetTypeInstEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facet_type_id", nullable = false)
    val facetTypeId: Long = 0,

    @Column(name = "uuid", nullable = false, length = 36)
    var uuid: String,

    @Column(name = "type_res", nullable = false, length = 512)
    var typeRes: String,

    @Column(name = "slug", length = 512)
    var slug: String?,

    @Column(name = "display_name", length = 512)
    var displayName: String?,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String?,

    @Column(name = "source", nullable = false, length = 32)
    var source: String = "OBSERVED",

    @ManyToOne(optional = true)
    @JoinColumn(
        name = "def_id",
        referencedColumnName = "def_id",
        foreignKey = ForeignKey(ConstraintMode.CONSTRAINT)
    )
    var facetTypeDef: MetadataFacetTypeDefEntity?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant,

    @Column(name = "last_modified_by", length = 255)
    var lastModifiedBy: String?
)
