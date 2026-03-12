package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataEntity

/**
 * Output of [SchemaFacetService.getSchemas].
 *
 * [schemas] contains all physical schemas, each enriched with any matched metadata.
 * [unboundMetadata] holds metadata entities whose coordinates did not match any physical
 * schema, table, or attribute — i.e. stale or orphaned metadata.
 */
data class SchemaFacetResult(
    /** All physical schemas, each enriched with matched metadata. Never empty — preserves every schema from the provider. */
    val schemas: List<SchemaWithFacets>,

    /** Metadata entities whose coordinates did not match any physical schema, table, or attribute.
     *  These are stale or orphaned entries that reference physical objects no longer present. */
    val unboundMetadata: List<MetadataEntity>
)
