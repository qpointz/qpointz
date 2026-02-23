package io.qpointz.mill.metadata.api.dto

/** REST DTO representing a single resolved facet payload. */
data class FacetDto(
    var facetType: String? = null,
    var data: Any? = null,
    var availableScopes: Set<String>? = null
)
