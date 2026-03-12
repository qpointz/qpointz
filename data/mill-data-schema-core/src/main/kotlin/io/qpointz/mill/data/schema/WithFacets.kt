package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataEntity

/**
 * Common contract for domain objects that carry schema-bound metadata facets.
 *
 * Guarantees that every [WithFacets] object exposes its matched metadata entity
 * and resolved facets, and can report whether metadata was present at all.
 */
interface WithFacets {
    /** Matched metadata entity, or null when no metadata entry exists for this physical object. */
    val metadata: MetadataEntity?

    /** Resolved facets for this object. [SchemaFacets.EMPTY] when [metadata] is null or carries no recognised facets. */
    val facets: SchemaFacets

    /** True when a metadata entity was matched; false when the physical object has no metadata coverage. */
    val hasMetadata: Boolean get() = metadata != null
}
