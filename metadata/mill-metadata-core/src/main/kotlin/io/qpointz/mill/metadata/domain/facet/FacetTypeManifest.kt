package io.qpointz.mill.metadata.domain.facet

import java.io.Serializable

/**
 * Canonical facet type definition manifest.
 *
 * This manifest is stored verbatim as JSON in persistence and served via the facet type
 * management API. Identifier slugs may be accepted at the boundary, but must be URN-normalized
 * before persistence and on all responses.
 *
 * Object payload fields are ordered via [payload.fields] for deterministic UI presentation.
 */
data class FacetTypeManifest(
    val typeKey: String,
    val title: String,
    val description: String,
    val category: String = "general",
    val enabled: Boolean = true,
    val mandatory: Boolean = false,
    val targetCardinality: FacetTargetCardinality = FacetTargetCardinality.SINGLE,
    val applicableTo: List<String>? = null,
    val schemaVersion: String? = null,
    val payload: FacetPayloadSchema
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

/** Declares whether a facet type can appear once or multiple times per target entity. */
enum class FacetTargetCardinality {
    SINGLE,
    MULTIPLE
}

