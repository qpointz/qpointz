package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SchemaField
import io.qpointz.mill.types.sql.DatabaseType
import org.apache.parquet.schema.LogicalTypeAnnotation
import org.apache.parquet.schema.MessageType
import org.apache.parquet.schema.PrimitiveType
import org.apache.parquet.schema.Type

/**
 * Converts a Parquet [MessageType] schema to a Mill [RecordSchema].
 *
 * Supports Parquet primitive types and common logical type annotations.
 * Nullability is derived from the Parquet repetition level (OPTIONAL vs REQUIRED).
 *
 * Type mapping:
 *
 * | Parquet primitive | Logical annotation | Mill DatabaseType |
 * |-------------------|-------------------|-------------------|
 * | BOOLEAN           | —                 | `bool`            |
 * | INT32             | —                 | `i32`             |
 * | INT64             | —                 | `i64`             |
 * | FLOAT             | —                 | `fp32`            |
 * | DOUBLE            | —                 | `fp64`            |
 * | BINARY            | STRING/UTF8       | `string`          |
 * | BINARY            | —                 | `binary`          |
 * | FIXED_LEN_BYTE_ARRAY | —             | `binary`          |
 * | INT96             | —                 | `binary`          |
 */
object ParquetSchemaConverter {

    /**
     * Converts a Parquet [MessageType] to a Mill [RecordSchema].
     */
    fun convert(parquetSchema: MessageType): RecordSchema {
        val fields = parquetSchema.fields.mapIndexed { idx, field ->
            val dbType = convertField(field)
            SchemaField(field.name, idx, dbType)
        }
        return RecordSchema(fields)
    }

    private fun convertField(field: Type): DatabaseType {
        require(field.isPrimitive) {
            "Nested/group types are not supported: ${field.name}"
        }
        val primitive = field.asPrimitiveType()
        val nullable = primitive.repetition != Type.Repetition.REQUIRED
        return convertPrimitive(primitive, nullable)
    }

    private fun convertPrimitive(type: PrimitiveType, nullable: Boolean): DatabaseType {
        val logicalType = type.logicalTypeAnnotation

        return when (type.primitiveTypeName) {
            PrimitiveType.PrimitiveTypeName.BOOLEAN -> DatabaseType.bool(nullable)
            PrimitiveType.PrimitiveTypeName.INT32 -> convertInt32(logicalType, nullable)
            PrimitiveType.PrimitiveTypeName.INT64 -> convertInt64(logicalType, nullable)
            PrimitiveType.PrimitiveTypeName.FLOAT -> DatabaseType.fp32(nullable, -1, -1)
            PrimitiveType.PrimitiveTypeName.DOUBLE -> DatabaseType.fp64(nullable, -1, -1)
            PrimitiveType.PrimitiveTypeName.BINARY -> convertBinary(logicalType, nullable)
            PrimitiveType.PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY ->
                DatabaseType.binary(nullable, type.typeLength)
            PrimitiveType.PrimitiveTypeName.INT96 ->
                DatabaseType.binary(nullable, 12)
            else -> throw IllegalArgumentException(
                "Unsupported Parquet primitive type: ${type.primitiveTypeName}"
            )
        }
    }

    private fun convertInt32(logicalType: LogicalTypeAnnotation?, nullable: Boolean): DatabaseType {
        return when (logicalType) {
            is LogicalTypeAnnotation.IntLogicalTypeAnnotation -> {
                when (logicalType.bitWidth) {
                    8 -> DatabaseType.i32(nullable)   // INT_8 → i32
                    16 -> DatabaseType.i16(nullable)  // INT_16 → i16
                    else -> DatabaseType.i32(nullable)
                }
            }
            is LogicalTypeAnnotation.DateLogicalTypeAnnotation -> DatabaseType.date(nullable)
            else -> DatabaseType.i32(nullable)
        }
    }

    private fun convertInt64(logicalType: LogicalTypeAnnotation?, nullable: Boolean): DatabaseType {
        return when (logicalType) {
            is LogicalTypeAnnotation.TimestampLogicalTypeAnnotation -> DatabaseType.of(
                io.qpointz.mill.types.logical.TimestampLogical.INSTANCE, nullable
            )
            is LogicalTypeAnnotation.TimeLogicalTypeAnnotation -> DatabaseType.time(nullable)
            else -> DatabaseType.i64(nullable)
        }
    }

    private fun convertBinary(logicalType: LogicalTypeAnnotation?, nullable: Boolean): DatabaseType {
        return when (logicalType) {
            is LogicalTypeAnnotation.StringLogicalTypeAnnotation -> DatabaseType.string(nullable, -1)
            is LogicalTypeAnnotation.EnumLogicalTypeAnnotation -> DatabaseType.string(nullable, -1)
            is LogicalTypeAnnotation.JsonLogicalTypeAnnotation -> DatabaseType.string(nullable, -1)
            else -> DatabaseType.binary(nullable, -1)
        }
    }
}
