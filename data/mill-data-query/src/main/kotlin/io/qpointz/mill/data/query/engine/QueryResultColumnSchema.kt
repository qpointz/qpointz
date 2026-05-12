package io.qpointz.mill.data.query.engine

import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.VectorBlockSchema

/**
 * Builds JSON-serializable column descriptors for the query-result HTTP envelope (`schema` array),
 * sourced from a [VectorBlockSchema] (same metadata as [io.qpointz.mill.proto.VectorBlock]).
 *
 * @receiver block or iterator schema from the dispatcher
 * @return ordered list of column maps (`idx` ascending)
 */
fun VectorBlockSchema.toQueryResultSchemaColumns(): List<Map<String, Any?>> =
    fieldsList
        .sortedBy { it.fieldIdx }
        .map { it.toQueryResultColumnDescriptor() }

/**
 * @return one column entry: `name`, `type`, `precision`, `scale`, `length`, `nullable`, `idx`
 */
private fun Field.toQueryResultColumnDescriptor(): Map<String, Any?> {
    val logical = type.type
    val typeId = logical.typeId
    val typeName =
        if (typeId == LogicalDataType.LogicalDataTypeId.NOT_SPECIFIED_TYPE) {
            "UNKNOWN"
        } else {
            typeId.name
        }
    val precisionVal = logical.precision
    val scaleVal = logical.scale
    val hasPrecision = precisionVal != 0
    val hasScale = scaleVal != 0
    val isString = typeId == LogicalDataType.LogicalDataTypeId.STRING
    val nullable: Boolean? =
        when (type.nullability) {
            DataType.Nullability.NULL -> true
            DataType.Nullability.NOT_NULL -> false
            DataType.Nullability.NOT_SPECIFIED_NULL,
            DataType.Nullability.UNRECOGNIZED,
            -> null
        }
    return linkedMapOf(
        "name" to name,
        "type" to typeName,
        "precision" to if (hasPrecision) precisionVal else null,
        "scale" to if (hasScale) scaleVal else null,
        "length" to if (isString && hasPrecision) precisionVal else null,
        "nullable" to nullable,
        "idx" to fieldIdx,
    )
}
