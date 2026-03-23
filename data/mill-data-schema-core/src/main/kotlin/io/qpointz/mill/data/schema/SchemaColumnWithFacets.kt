package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.proto.DataType

/**
 * Physical column enriched with metadata facets.
 *
 * All physical properties from the source [io.qpointz.mill.proto.Field] are preserved.
 * [metadata] is null when no metadata entity was matched for this column.
 * [facets] is [SchemaFacets.EMPTY] when metadata is absent or carries no recognized facets.
 *
 * @property schemaName physical schema name that owns the column
 * @property tableName physical table name that owns the column
 * @property columnName physical column name
 * @property fieldIndex zero-based column index from the physical schema
 * @property dataType physical Mill data type descriptor
 * @property metadata matched metadata entity, or null when no match exists
 * @property facets resolved facet holder for this column
 */
data class SchemaColumnWithFacets(
    val schemaName: String,
    val tableName: String,
    val columnName: String,
    val fieldIndex: Int,
    val dataType: DataType,
    override val metadata: MetadataEntity?,
    override val facets: SchemaFacets
) : WithFacets
