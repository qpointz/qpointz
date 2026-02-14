package io.qpointz.mill.source.format.avro

import io.qpointz.mill.source.FlowRecordWriter
import io.qpointz.mill.source.Record
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import java.io.OutputStream
import org.apache.avro.Schema as AvroSchema

/**
 * Settings for [AvroRecordWriter].
 *
 * @property schemaSource provides the Avro schema for the output file
 */
data class AvroWriterSettings(
    val schemaSource: AvroSchemaSource
)

/**
 * Writes Mill [Record] instances as an Avro data file.
 *
 * Usage:
 * ```
 * val writer = AvroRecordWriter(settings, outputStream)
 * writer.use {
 *     it.open()
 *     records.forEach { r -> it.write(r) }
 * }
 * ```
 *
 * @property settings writer configuration including the Avro schema
 * @property outputStream the destination for Avro output
 */
class AvroRecordWriter(
    private val settings: AvroWriterSettings,
    private val outputStream: OutputStream
) : FlowRecordWriter {

    private var avroSchema: AvroSchema? = null
    private var dataFileWriter: DataFileWriter<GenericRecord>? = null

    override fun open() {
        val schema = settings.schemaSource.schema()
        avroSchema = schema
        val datumWriter = GenericDatumWriter<GenericRecord>(schema)
        val dfw = DataFileWriter(datumWriter)
        dfw.create(schema, outputStream)
        dataFileWriter = dfw
    }

    override fun write(record: Record) {
        val schema = avroSchema ?: throw IllegalStateException("Writer not opened. Call open() first.")
        val dfw = dataFileWriter ?: throw IllegalStateException("Writer not opened. Call open() first.")

        val avroRecord = GenericData.Record(schema)
        for (field in schema.fields) {
            val value = record[field.name()]
            avroRecord.put(field.name(), convertToAvro(value, field.schema()))
        }
        dfw.append(avroRecord)
    }

    override fun close() {
        dataFileWriter?.close()
        dataFileWriter = null
        avroSchema = null
    }

    companion object {

        /**
         * Converts a Mill value to an Avro-compatible value.
         *
         * Handles union types by unwrapping the non-null branch.
         */
        internal fun convertToAvro(value: Any?, fieldSchema: AvroSchema): Any? {
            if (value == null) return null

            val effectiveSchema = when (fieldSchema.type) {
                AvroSchema.Type.UNION -> {
                    fieldSchema.types.firstOrNull { it.type != AvroSchema.Type.NULL }
                        ?: fieldSchema
                }
                else -> fieldSchema
            }

            return when (effectiveSchema.type) {
                AvroSchema.Type.STRING -> value.toString()
                AvroSchema.Type.BYTES -> when (value) {
                    is ByteArray -> java.nio.ByteBuffer.wrap(value)
                    else -> java.nio.ByteBuffer.wrap(value.toString().toByteArray())
                }
                AvroSchema.Type.INT -> (value as? Number)?.toInt() ?: value
                AvroSchema.Type.LONG -> (value as? Number)?.toLong() ?: value
                AvroSchema.Type.FLOAT -> (value as? Number)?.toFloat() ?: value
                AvroSchema.Type.DOUBLE -> (value as? Number)?.toDouble() ?: value
                AvroSchema.Type.BOOLEAN -> value as? Boolean ?: value
                else -> value
            }
        }
    }
}
