package io.qpointz.mill.source.format.avro

import org.apache.avro.Schema as AvroSchema

/**
 * Provides an Avro [AvroSchema] for writing operations.
 *
 * Writers need an explicit Avro schema (unlike readers, which infer it
 * from the file). Implementations supply the schema from different origins.
 */
interface AvroSchemaSource {

    /** Returns the Avro schema to use. */
    fun schema(): AvroSchema
}

/**
 * Returns a fixed Avro schema provided at construction time.
 *
 * @property avroSchema the constant schema
 */
class ConstantSchemaSource(private val avroSchema: AvroSchema) : AvroSchemaSource {
    override fun schema(): AvroSchema = avroSchema
}

/**
 * Parses an Avro schema from a JSON string.
 *
 * @property json the Avro schema in JSON format
 */
class JsonSchemaSource(private val json: String) : AvroSchemaSource {
    override fun schema(): AvroSchema = AvroSchema.Parser().parse(json)
}
