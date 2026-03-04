package io.qpointz.mill.metadata.api.dto

import io.qpointz.mill.metadata.domain.MetadataType

data class SearchResultDto(
    val id: String?,
    val name: String?,
    val type: MetadataType?,
    val displayName: String?,
    val description: String?,
    val location: String?,
    val score: Double? = null
)
