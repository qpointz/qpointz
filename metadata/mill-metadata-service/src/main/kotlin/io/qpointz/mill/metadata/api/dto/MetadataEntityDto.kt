package io.qpointz.mill.metadata.api.dto

import io.qpointz.mill.metadata.domain.MetadataType
import java.time.Instant

/** REST DTO exposing metadata entity fields plus scope-resolved facets. */
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
