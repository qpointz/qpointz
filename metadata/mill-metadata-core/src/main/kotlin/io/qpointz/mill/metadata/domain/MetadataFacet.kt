package io.qpointz.mill.metadata.domain

/**
 * Marker for schema-shaped facet payloads: only the stable type key is shared; there is no
 * merge/validate/setOwner lifecycle (SPEC §2).
 */
interface MetadataFacet {
    /** Stable type key used in persisted facet maps (for example `descriptive`). */
    val facetType: String
}
