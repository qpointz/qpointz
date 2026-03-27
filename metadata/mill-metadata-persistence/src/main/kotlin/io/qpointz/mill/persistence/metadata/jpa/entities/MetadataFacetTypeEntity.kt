package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * JPA entity representing a facet type descriptor row in `metadata_facet_type_def`.
 *
 * Platform facet types are seeded by Flyway. Custom types are inserted by the import service
 * or by `POST /api/v1/metadata/facets`.
 *
 * @property facetTypeDefId Surrogate primary key.
 * @property typeRes        Full Mill facet-type URN (former `type_key`).
 * @property mandatory      Whether the type is required on applicable entities
 * @property enabled        Whether the type is active and visible
 * @property displayName    Human-readable label
 * @property description   Optional longer description
 * @property applicableToJson  JSON array of entity-type URN strings; `[]` means all types
 * @property version       Optional schema version string
 * @property contentSchemaJson Optional JSON object defining validation rules for facet payloads
 * @property manifestJson  Canonical facet type manifest JSON (stored verbatim)
 * @property createdAt     Creation timestamp
 * @property updatedAt     Last-modified timestamp
 * @property createdBy     Actor who created this descriptor
 * @property updatedBy     Actor who last modified this descriptor
 */
@Entity
@Table(name = "metadata_facet_type_def")
class MetadataFacetTypeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facet_type_def_id", nullable = false)
    val facetTypeDefId: Long = 0,

    @Column(name = "type_res", nullable = false, length = 255)
    var typeRes: String,

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

    @Column(name = "manifest_json", nullable = false, columnDefinition = "TEXT")
    var manifestJson: String = "{}",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "created_by", length = 255)
    var createdBy: String?,

    @Column(name = "updated_by", length = 255)
    var updatedBy: String?
)
