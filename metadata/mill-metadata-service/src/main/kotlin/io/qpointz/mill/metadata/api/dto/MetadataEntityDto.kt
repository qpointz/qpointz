package io.qpointz.mill.metadata.api.dto

import io.qpointz.mill.metadata.domain.MetadataType
import java.time.Instant

/**
 * REST DTO for a metadata entity, exposing entity coordinates and scope-resolved facets.
 *
 * Returned by all entity read endpoints. The `facets` map keys are full facet-type URN strings;
 * the values are the raw two-level scope maps (scope URN → payload) as stored.
 *
 * @property id            entity identifier string
 * @property type          entity type classification (SCHEMA, TABLE, ATTRIBUTE, CONCEPT, CATALOG)
 * @property schemaName    schema name coordinate; present for SCHEMA, TABLE, and ATTRIBUTE entities
 * @property tableName     table name coordinate; present for TABLE and ATTRIBUTE entities
 * @property attributeName attribute name coordinate; present for ATTRIBUTE entities
 * @property createdAt     creation timestamp
 * @property updatedAt     last-modified timestamp
 * @property createdBy     actor who created the entity
 * @property updatedBy     actor who last modified the entity
 * @property facets        two-level facet map: facet-type URN → scope URN → payload; may be null
 */
data class MetadataEntityDto(
    var id: String? = null,
    var type: MetadataType? = null,
    var schemaName: String? = null,
    var tableName: String? = null,
    var attributeName: String? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    var createdBy: String? = null,
    var updatedBy: String? = null,
    var facets: Map<String, Any?>? = null
)
