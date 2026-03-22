package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * JPA entity representing a metadata entity row in `metadata_entity`.
 *
 * Stores the identity and coordinates of a single schema object (schema, table, attribute,
 * or concept). Facet data is stored separately in [MetadataFacetScopeEntity] rows linked
 * via [facetScopes].
 *
 * The combination `(schema_name, table_name, attribute_name)` is guaranteed unique by a
 * database constraint. Any of the three may be null; a schema-level entity has only
 * `schema_name` set.
 *
 * @property entityId     primary key; matches [io.qpointz.mill.metadata.domain.MetadataEntity.id]
 * @property entityType   serialised [io.qpointz.mill.metadata.domain.MetadataType] name
 * @property schemaName   schema coordinate; present for SCHEMA, TABLE, and ATTRIBUTE entities
 * @property tableName    table coordinate; present for TABLE and ATTRIBUTE entities
 * @property attributeName attribute coordinate; present for ATTRIBUTE entities
 * @property createdAt    creation timestamp
 * @property updatedAt    last-modified timestamp
 * @property createdBy    actor who created this entity
 * @property updatedBy    actor who last modified this entity
 * @property facetScopes  associated [MetadataFacetScopeEntity] rows, owned with cascade all
 */
@Entity
@Table(
    name = "metadata_entity",
    indexes = [
        Index(name = "idx_metadata_entity_type", columnList = "entity_type")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_metadata_entity_location",
            columnNames = ["schema_name", "table_name", "attribute_name"]
        )
    ]
)
class MetadataEntityRecord(

    @Id
    @Column(name = "entity_id", nullable = false, length = 255)
    val entityId: String,

    @Column(name = "entity_type", nullable = false, length = 64)
    var entityType: String,

    @Column(name = "schema_name", length = 512)
    var schemaName: String?,

    @Column(name = "table_name", length = 512)
    var tableName: String?,

    @Column(name = "attribute_name", length = 512)
    var attributeName: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "updated_by", length = 255)
    var updatedBy: String?,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @jakarta.persistence.JoinColumn(name = "entity_id")
    val facetScopes: MutableList<MetadataFacetScopeEntity> = mutableListOf()
)
