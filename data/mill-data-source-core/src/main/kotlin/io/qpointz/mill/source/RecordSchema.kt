package io.qpointz.mill.source

import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.VectorBlockSchema
import io.qpointz.mill.types.sql.DatabaseType

/**
 * Describes a single field (column) in a [RecordSchema].
 *
 * @property name   the column name
 * @property index  zero-based ordinal position within the schema
 * @property type   the mill-core [DatabaseType] for this column
 */
data class SchemaField(
    val name: String,
    val index: Int,
    val type: DatabaseType
) {

    /**
     * Converts this field to a protobuf [Field] message.
     */
    fun toProtoField(): Field = Field.newBuilder()
        .setName(name)
        .setFieldIdx(index)
        .setType(type.asDataType())
        .build()
}

/**
 * Schema describing the structure of records or vector blocks produced by a source.
 *
 * Each field has a name, ordinal index, and a [DatabaseType].
 * The schema can be converted to a protobuf [VectorBlockSchema] for
 * integration with mill-core's columnar vector path.
 *
 * @property fields ordered list of [SchemaField] entries
 */
data class RecordSchema(val fields: List<SchemaField>) {

    /**
     * Returns the number of fields in this schema.
     */
    val size: Int get() = fields.size

    /**
     * Looks up a field by name, or `null` if not found.
     */
    fun field(name: String): SchemaField? = fieldsByName[name]

    /**
     * Looks up a field by zero-based index, or `null` if out of bounds.
     */
    fun field(index: Int): SchemaField? = if (index in fields.indices) fields[index] else null

    /**
     * Returns the list of field names in order.
     */
    val fieldNames: List<String> get() = fields.map { it.name }

    /**
     * Converts this schema to a protobuf [VectorBlockSchema] message.
     */
    fun toVectorBlockSchema(): VectorBlockSchema = VectorBlockSchema.newBuilder()
        .addAllFields(fields.map { it.toProtoField() })
        .build()

    private val fieldsByName: Map<String, SchemaField> by lazy {
        fields.associateBy { it.name }
    }

    companion object {

        /**
         * Creates a [RecordSchema] from vararg name-type pairs.
         * Indexes are assigned sequentially starting from 0.
         *
         * Example:
         * ```
         * val schema = RecordSchema.of(
         *     "id" to DatabaseType.i32(false),
         *     "name" to DatabaseType.string(true, -1)
         * )
         * ```
         */
        fun of(vararg fields: Pair<String, DatabaseType>): RecordSchema =
            RecordSchema(fields.mapIndexed { idx, (name, type) -> SchemaField(name, idx, type) })

        /**
         * Creates an empty [RecordSchema] with no fields.
         */
        fun empty(): RecordSchema = RecordSchema(emptyList())
    }
}
