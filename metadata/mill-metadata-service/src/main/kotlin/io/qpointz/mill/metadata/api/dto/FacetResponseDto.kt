package io.qpointz.mill.metadata.api.dto

/**
 * Wrapper response DTO for a single facet type's merged payload.
 *
 * All facet read endpoints return this DTO to provide a consistent envelope containing both
 * the facet type identifier and the resolved payload.
 *
 * @property facetType the full URN key of the facet type (e.g.
 *                     `urn:mill/metadata/facet-type:descriptive`)
 * @property payload   the merged facet payload for the requested context;
 *                     `null` if no data is available under any scope in the context
 */
data class FacetResponseDto(
    val facetType: String,
    val payload: Any?
)
