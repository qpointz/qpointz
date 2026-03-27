package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

/**
 * Facet payload instance row in `metadata_facet`.
 */
@Entity
@Table(name = "metadata_facet")
class MetadataFacetEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facet_id", nullable = false)
    val facetId: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "entity_id",
        referencedColumnName = "entity_id",
        foreignKey = ForeignKey(ConstraintMode.CONSTRAINT)
    )
    var entity: MetadataEntityRecord,

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "scope_id",
        referencedColumnName = "scope_id",
        foreignKey = ForeignKey(ConstraintMode.CONSTRAINT)
    )
    var scope: MetadataScopeEntity,

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "facet_type_id",
        referencedColumnName = "facet_type_id",
        foreignKey = ForeignKey(ConstraintMode.CONSTRAINT)
    )
    var facetType: MetadataFacetTypeInstEntity,

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    var payloadJson: String = "{}",

    /**
     * Stable UUID string for this payload row; exposed in facet list/delete APIs.
     */
    @Column(name = "facet_uid", nullable = false, length = 36, unique = true)
    var facetUid: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "updated_by", length = 255)
    var updatedBy: String?
)
