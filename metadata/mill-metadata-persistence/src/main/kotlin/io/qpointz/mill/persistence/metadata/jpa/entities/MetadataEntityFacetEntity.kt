package io.qpointz.mill.persistence.metadata.jpa.entities

import io.qpointz.mill.persistence.metadata.jpa.listeners.MetadataEntityFacetAuditListener
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
 * JPA row for `metadata_entity_facet`.
 */
@Entity
@Table(
    name = "metadata_entity_facet",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_metadata_entity_facet_uuid", columnNames = ["uuid"])
    ]
)
@EntityListeners(MetadataEntityFacetAuditListener::class)
class MetadataEntityFacetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facet_id", nullable = false)
    val facetId: Long = 0,

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    var uuid: String,

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "entity_id",
        referencedColumnName = "entity_id",
        foreignKey = ForeignKey(ConstraintMode.CONSTRAINT)
    )
    var entity: MetadataEntityRecord,

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "facet_type_id",
        referencedColumnName = "facet_type_id",
        foreignKey = ForeignKey(ConstraintMode.CONSTRAINT)
    )
    var facetType: MetadataFacetTypeInstEntity,

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "scope_id",
        referencedColumnName = "scope_id",
        foreignKey = ForeignKey(ConstraintMode.CONSTRAINT)
    )
    var scope: MetadataScopeEntity,

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    var payloadJson: String = "{}",

    @Column(name = "merge_action", nullable = false, length = 32)
    var mergeAction: String = "SET",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant,

    @Column(name = "last_modified_by", length = 255)
    var lastModifiedBy: String?
)
