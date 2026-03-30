package io.qpointz.mill.metadata.api.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * REST DTO mirroring [io.qpointz.mill.metadata.domain.FacetTypeDescriptor].
 *
 * Used for all facet type catalog endpoints. All `applicableTo` values are full URN strings
 * (e.g. `urn:mill/metadata/entity-type:table`).
 *
 * @property facetTypeUrn  unique URN key for this facet type (JSON name; legacy `typeKey` accepted)
 * @property mandatory     whether the facet type is required on applicable entities
 * @property enabled       whether this facet type is active
 * @property displayName   human-readable label
 * @property description   optional description
 * @property applicableTo  set of entity-type URN strings this type applies to;
 *                         null or empty means applicable to all
 * @property version       optional schema version
 * @property contentSchema optional JSON-schema validation rules
 * @property createdAt     creation timestamp
 * @property updatedAt     last-modified timestamp
 * @property createdBy     actor who created the descriptor
 * @property updatedBy     actor who last modified the descriptor
 */
data class FacetTypeDescriptorDto(
    @param:JsonProperty("facetTypeUrn")
    @param:JsonAlias("typeKey")
    var facetTypeUrn: String = "",
    var mandatory: Boolean = false,
    var enabled: Boolean = true,
    var displayName: String? = null,
    var description: String? = null,
    var applicableTo: Set<String>? = null,
    var version: String? = null,
    var contentSchema: Map<String, Any?>? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    var createdBy: String? = null,
    var updatedBy: String? = null
)
