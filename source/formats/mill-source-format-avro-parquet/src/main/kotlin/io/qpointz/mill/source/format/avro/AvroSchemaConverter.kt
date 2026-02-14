package io.qpointz.mill.source.format.avro

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SchemaField
import io.qpointz.mill.types.sql.DatabaseType
import org.apache.avro.Schema as AvroSchema

/**
 * Converts an Apache Avro [AvroSchema] to a Mill [RecordSchema].
 *
 * Supports Avro primitive types and nullable unions (`["null", <type>]`).
 * Complex types (arrays, maps, nested records) are not currently supported
 * and will throw [IllegalArgumentException].
 *
 * Type mapping:
 *
 * | Avro Type   | Mill DatabaseType                |
 * |-------------|----------------------------------|
 * | BOOLEAN     | `bool(nullable)`                 |
 * | INT         | `i32(nullable)`                  |
 * | LONG        | `i64(nullable)`                  |
 * | FLOAT       | `fp32(nullable, -1, -1)`         |
 * | DOUBLE      | `fp64(nullable, -1, -1)`         |
 * | STRING      | `string(nullable, -1)`           |
 * | BYTES       | `binary(nullable, -1)`           |
 * | FIXED       | `binary(nullable, size)`         |
 * | ENUM        | `string(nullable, -1)`           |
 * | UNION       | nullable variant of inner type   |
 */
object AvroSchemaConverter {

    /**
     * Converts an Avro record [schema] to a Mill [RecordSchema].
     *
     * @param schema an Avro schema of type RECORD
     * @return the equivalent Mill [RecordSchema]
     * @throws IllegalArgumentException if [schema] is not a RECORD type
     */
    fun convert(schema: AvroSchema): RecordSchema {
        require(schema.type == AvroSchema.Type.RECORD) {
            "Expected Avro RECORD schema, got ${schema.type}"
        }
        val fields = schema.fields.mapIndexed { idx, field ->
            val (dbType, _) = convertFieldType(field.schema())
            SchemaField(field.name(), idx, dbType)
        }
        return RecordSchema(fields)
    }

    /**
     * Converts a single Avro field schema to a Mill [DatabaseType].
     *
     * @param schema the Avro schema for the field
     * @return a pair of (DatabaseType, nullable flag)
     */
    internal fun convertFieldType(schema: AvroSchema): Pair<DatabaseType, Boolean> {
        return when (schema.type) {
            AvroSchema.Type.UNION -> convertUnion(schema)
            else -> Pair(convertPrimitive(schema, false), false)
        }
    }

    private fun convertUnion(schema: AvroSchema): Pair<DatabaseType, Boolean> {
        val nonNullTypes = schema.types.filter { it.type != AvroSchema.Type.NULL }
        val nullable = schema.types.any { it.type == AvroSchema.Type.NULL }

        return when {
            nonNullTypes.size == 1 -> Pair(convertPrimitive(nonNullTypes[0], nullable), nullable)
            nonNullTypes.isEmpty() -> Pair(DatabaseType.string(true, -1), true)
            else -> throw IllegalArgumentException(
                "Complex unions with multiple non-null types are not supported: ${schema.types.map { it.type }}"
            )
        }
    }

    private fun convertPrimitive(schema: AvroSchema, nullable: Boolean): DatabaseType {
        return when (schema.type) {
            AvroSchema.Type.BOOLEAN -> DatabaseType.bool(nullable)
            AvroSchema.Type.INT -> DatabaseType.i32(nullable)
            AvroSchema.Type.LONG -> DatabaseType.i64(nullable)
            AvroSchema.Type.FLOAT -> DatabaseType.fp32(nullable, -1, -1)
            AvroSchema.Type.DOUBLE -> DatabaseType.fp64(nullable, -1, -1)
            AvroSchema.Type.STRING -> DatabaseType.string(nullable, -1)
            AvroSchema.Type.BYTES -> DatabaseType.binary(nullable, -1)
            AvroSchema.Type.FIXED -> DatabaseType.binary(nullable, schema.fixedSize)
            AvroSchema.Type.ENUM -> DatabaseType.string(nullable, -1)
            else -> throw IllegalArgumentException(
                "Unsupported Avro type: ${schema.type}" +
                if (schema.name != null) " (field: ${schema.name})" else ""
            )
        }
    }
}
