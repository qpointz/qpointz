package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.FlowRecordWriter
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.format.avro.AvroRecordWriter
import io.qpointz.mill.source.format.avro.AvroSchemaSource
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path as HadoopPath
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.avro.Schema as AvroSchema
import java.nio.file.Path

/**
 * Settings for [ParquetRecordWriter].
 *
 * @property schemaSource   provides the Avro schema for the output file
 * @property compression    compression codec (default: SNAPPY)
 */
data class ParquetWriterSettings(
    val schemaSource: AvroSchemaSource,
    val compression: CompressionCodecName = CompressionCodecName.SNAPPY
)

/**
 * Writes Mill [Record] instances as a Parquet file.
 *
 * Uses the Avro-based Parquet writer (`parquet-avro`) under the hood.
 * Records are converted to Avro [GenericRecord] instances before writing.
 *
 * Usage:
 * ```
 * val writer = ParquetRecordWriter(settings, outputPath)
 * writer.use {
 *     it.open()
 *     records.forEach { r -> it.write(r) }
 * }
 * ```
 *
 * @property settings   writer configuration including Avro schema and compression
 * @property outputPath filesystem path for the output Parquet file
 */
class ParquetRecordWriter(
    private val settings: ParquetWriterSettings,
    private val outputPath: Path
) : FlowRecordWriter {

    private var avroSchema: AvroSchema? = null
    private var parquetWriter: ParquetWriter<GenericRecord>? = null

    override fun open() {
        val schema = settings.schemaSource.schema()
        avroSchema = schema
        val hadoopPath = HadoopPath(outputPath.toUri())
        val conf = Configuration()
        conf.setBoolean("fs.local.block.write.checksum", false)
        parquetWriter = AvroParquetWriter.builder<GenericRecord>(hadoopPath)
            .withSchema(schema)
            .withCompressionCodec(settings.compression)
            .withConf(conf)
            .build()
    }

    override fun write(record: Record) {
        val schema = avroSchema ?: throw IllegalStateException("Writer not opened. Call open() first.")
        val writer = parquetWriter ?: throw IllegalStateException("Writer not opened. Call open() first.")

        val avroRecord = GenericData.Record(schema)
        for (field in schema.fields) {
            val value = record[field.name()]
            avroRecord.put(field.name(), AvroRecordWriter.convertToAvro(value, field.schema()))
        }
        writer.write(avroRecord)
    }

    override fun close() {
        parquetWriter?.close()
        parquetWriter = null
        avroSchema = null
    }
}
