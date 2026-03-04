package io.qpointz.mill.metadata.api.dto

import io.qpointz.mill.metadata.domain.MetadataType

data class TreeNodeDto(
    val id: String?,
    val name: String?,
    val type: MetadataType?,
    val displayName: String?,
    val description: String? = null,
    var children: List<TreeNodeDto>? = null,
    var hasChildren: Boolean = false
)
