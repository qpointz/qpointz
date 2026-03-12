package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataEntity

/**
 * Physical schema enriched with metadata facets.
 *
 * [metadata] is null when no metadata entity was matched at the schema level.
 * [facets] is [SchemaFacets.EMPTY] when metadata is absent or carries no recognized facets.
 * All tables from the physical schema are preserved in [tables] regardless of metadata coverage.
 */
data class SchemaWithFacets(
    val schemaName: String,
    val tables: List<SchemaTableWithFacets>,
    override val metadata: MetadataEntity?,
    override val facets: SchemaFacets
) : WithFacets
