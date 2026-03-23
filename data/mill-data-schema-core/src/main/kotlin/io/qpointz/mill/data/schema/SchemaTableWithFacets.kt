package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.proto.Table

/**
 * Physical table enriched with metadata facets.
 *
 * All physical properties from the source [io.qpointz.mill.proto.Table] are preserved.
 * [metadata] is null when no metadata entity was matched for this table.
 * [facets] is [SchemaFacets.EMPTY] when metadata is absent or carries no recognized facets.
 */
data class SchemaTableWithFacets(
    val schemaName: String,
    val tableName: String,
    val tableType: Table.TableTypeId,
    val columns: List<SchemaColumnWithFacets>,
    override val metadata: MetadataEntity?,
    override val facets: SchemaFacets
) : WithFacets
