package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.proto.DataType

/**
 * Physical attribute enriched with metadata facets.
 *
 * All physical properties from the source [io.qpointz.mill.proto.Field] are preserved.
 * [metadata] is null when no metadata entity was matched for this attribute.
 * [facets] is [SchemaFacets.EMPTY] when metadata is absent or carries no recognized facets.
 */
data class SchemaAttributeWithFacets(
    val schemaName: String,
    val tableName: String,
    val attributeName: String,
    val fieldIndex: Int,
    val dataType: DataType,
    override val metadata: MetadataEntity?,
    override val facets: SchemaFacets
) : WithFacets
