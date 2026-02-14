package io.qpointz.mill.source.format.parquet

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.conf.Configuration
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import java.nio.file.Path

/**
 * Test utilities for creating Parquet test files via the Avro-based writer.
 */
object ParquetTestUtils {

    /** Standard test Avro schema: id (long), name (string, nullable), score (double), active (boolean) */
    val TEST_AVRO_SCHEMA: Schema = SchemaBuilder.record("TestRecord")
        .namespace("test")
        .fields()
        .requiredLong("id")
        .name("name").type().nullable().stringType().noDefault()
        .requiredDouble("score")
        .requiredBoolean("active")
        .endRecord()

    /**
     * Creates sample [GenericRecord] instances.
     */
    fun createTestRecords(): List<GenericRecord> {
        return listOf(
            GenericData.Record(TEST_AVRO_SCHEMA).apply {
                put("id", 1L)
                put("name", "Alice")
                put("score", 95.5)
                put("active", true)
            },
            GenericData.Record(TEST_AVRO_SCHEMA).apply {
                put("id", 2L)
                put("name", null)
                put("score", 82.0)
                put("active", false)
            },
            GenericData.Record(TEST_AVRO_SCHEMA).apply {
                put("id", 3L)
                put("name", "Charlie")
                put("score", 77.3)
                put("active", true)
            }
        )
    }

    /**
     * Writes a Parquet file using the Avro-based Parquet writer.
     */
    fun writeParquetFile(dir: Path, filename: String, records: List<GenericRecord>): Path {
        val filePath = dir.resolve(filename)
        val hadoopPath = org.apache.hadoop.fs.Path(filePath.toUri())
        val conf = Configuration()
        conf.setBoolean("fs.file.impl.disable.cache", true)
        conf.setBoolean("fs.local.block.write.checksum", false)
        val writer: ParquetWriter<GenericRecord> = AvroParquetWriter.builder<GenericRecord>(hadoopPath)
            .withSchema(TEST_AVRO_SCHEMA)
            .withCompressionCodec(CompressionCodecName.SNAPPY)
            .withConf(conf)
            .build()

        for (record in records) {
            writer.write(record)
        }
        writer.close()
        return filePath
    }
}
