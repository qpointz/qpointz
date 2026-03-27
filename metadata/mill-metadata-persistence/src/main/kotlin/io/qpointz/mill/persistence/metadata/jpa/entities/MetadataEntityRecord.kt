package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * JPA entity representing a metadata entity row in `metadata_entity`.
 *
 * Stores the identity and coordinates of a single schema object (schema, table, attribute,
 * or concept).
 *
 * The combination `(schema_name, table_name, attribute_name)` is guaranteed unique by a
 * database constraint. Any of the three may be null; a schema-level entity has only
 * `schema_name` set.
 *
 * @property entityId     Surrogate primary key.
 * @property entityRes    Business id / FQDN (former `entity_id` string); matches [io.qpointz.mill.metadata.domain.MetadataEntity.id].
 * @property entityType   Serialised [io.qpointz.mill.metadata.domain.MetadataType] name
 * @property schemaName   Schema coordinate; present for SCHEMA, TABLE, and ATTRIBUTE entities
 * @property tableName    Table coordinate; present for TABLE and ATTRIBUTE entities
 * @property attributeName Attribute coordinate; present for ATTRIBUTE entities
 * @property createdAt    Creation timestamp
 * @property updatedAt    Last-modified timestamp
 * @property createdBy    Actor who created this entity
 * @property updatedBy    Actor who last modified this entity
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entity_id", nullable = false)
    val entityId: Long = 0,

    @Column(name = "entity_res", nullable = false, length = 255)
    var entityRes: String,

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
    var updatedBy: String?
)
