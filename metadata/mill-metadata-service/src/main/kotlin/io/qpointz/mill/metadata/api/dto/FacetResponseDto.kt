package io.qpointz.mill.metadata.api.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Wrapper response DTO for a single facet type's merged payload.
 *
 * Facet read endpoints return this envelope with both the facet type identifier and the
 * resolved payload. `GET .../entities/{id}/facets` returns a **JSON array** of these objects
 * (so the same [facetType] may appear more than once when the service supports multiple
 * instances per type).
 *
 * @property facetType the full URN key of the facet type (e.g.
 *                     `urn:mill/metadata/facet-type:descriptive`)
 * @property uid       stable UUID for this persisted facet row when using JPA storage; absent for file-backed metadata
 * @property payload   the merged facet payload for the requested context;
 *                     `null` if no data is available under any scope in the context
 */
@Schema(
    name = "FacetResponse",
    description = "Facet type URN plus context-merged payload. " +
        "`GET /api/v1/metadata/entities/{id}/facets` returns `FacetResponse[]` (not a map keyed by type)."
)
data class FacetResponseDto(
    @field:Schema(
        description = "Full facet type URN",
        example = "urn:mill/metadata/facet-type:descriptive",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val facetType: String,
    @field:Schema(
        description = "Stable UUID of the facet instance row (JPA); omit for file-backed metadata",
        nullable = true
    )
    val uid: String? = null,
    @field:Schema(
        description = "Merged payload for the requested context; structure depends on facet type",
        nullable = true
    )
    val payload: Any?
)
