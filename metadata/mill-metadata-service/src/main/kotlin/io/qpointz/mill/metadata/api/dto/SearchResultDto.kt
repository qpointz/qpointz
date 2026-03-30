package io.qpointz.mill.metadata.api.dto

data class SearchResultDto(
    val id: String?,
    val name: String?,
    val kind: String?,
    val displayName: String?,
    val description: String?,
    val location: String?,
    val score: Double? = null
)
