package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * JPA entity representing a facet type descriptor row in `metadata_facet_type`.
 *
 * All five platform facet types are seeded by Flyway V4. Custom types are inserted by the
 * import service or by `POST /api/v1/metadata/facets`.
 *
 * The [applicableToJson] and [contentSchemaJson] columns store JSON arrays/objects.
 * The adapters in [io.qpointz.mill.persistence.metadata.jpa.adapters.JpaFacetTypeRepository]
 * handle de/serialisation using Jackson.
 *
 * @property typeKey           full Mill facet-type URN; primary key
 * @property mandatory         whether the type is required on applicable entities
 * @property enabled           whether the type is active and visible
 * @property displayName       human-readable label
 * @property description       optional longer description
 * @property applicableToJson  JSON array of entity-type URN strings; `[]` means all types
 * @property version           optional schema version string
 * @property contentSchemaJson optional JSON object defining validation rules for facet payloads
 * @property createdAt         creation timestamp
 * @property updatedAt         last-modified timestamp
 * @property createdBy         actor who created this descriptor
 * @property updatedBy         actor who last modified this descriptor
 */
@Entity
@Table(name = "metadata_facet_type")
class MetadataFacetTypeEntity(

    @Id
    @Column(name = "type_key", nullable = false, length = 255)
    val typeKey: String,

    @Column(name = "mandatory", nullable = false)
    var mandatory: Boolean = false,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Column(name = "display_name", length = 512)
    var displayName: String?,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String?,

    @Column(name = "applicable_to_json", nullable = false, columnDefinition = "TEXT")
    var applicableToJson: String = "[]",

    @Column(name = "version", length = 64)
    var version: String?,

    @Column(name = "content_schema_json", columnDefinition = "TEXT")
    var contentSchemaJson: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "updated_by", length = 255)
    var updatedBy: String?
)
