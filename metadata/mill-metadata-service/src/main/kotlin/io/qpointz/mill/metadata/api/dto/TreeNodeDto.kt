package io.qpointz.mill.metadata.api.dto

data class TreeNodeDto(
    val id: String?,
    val name: String?,
    val kind: String?,
    val displayName: String?,
    val description: String? = null,
    var children: List<TreeNodeDto>? = null,
    var hasChildren: Boolean = false
)
