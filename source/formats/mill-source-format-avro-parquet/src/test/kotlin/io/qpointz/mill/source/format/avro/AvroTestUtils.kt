package io.qpointz.mill.source.format.avro

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * Test utilities for creating in-memory Avro data.
 */
object AvroTestUtils {

    /** Standard test schema: id (long), name (string, nullable), score (double), active (boolean) */
    val TEST_SCHEMA: Schema = SchemaBuilder.record("TestRecord")
        .namespace("test")
        .fields()
        .requiredLong("id")
        .name("name").type().nullable().stringType().noDefault()
        .requiredDouble("score")
        .requiredBoolean("active")
        .endRecord()

    /**
     * Creates an Avro file in memory and returns it as an [InputStream].
     */
    fun createAvroInputStream(schema: Schema, records: List<GenericRecord>): InputStream {
        val baos = ByteArrayOutputStream()
        val writer = GenericDatumWriter<GenericRecord>(schema)
        val dataFileWriter = DataFileWriter(writer)
        dataFileWriter.create(schema, baos)
        for (record in records) {
            dataFileWriter.append(record)
        }
        dataFileWriter.close()
        return ByteArrayInputStream(baos.toByteArray())
    }

    /**
     * Creates sample [GenericRecord] instances using [TEST_SCHEMA].
     */
    fun createTestRecords(): List<GenericRecord> {
        return listOf(
            GenericData.Record(TEST_SCHEMA).apply {
                put("id", 1L)
                put("name", "Alice")
                put("score", 95.5)
                put("active", true)
            },
            GenericData.Record(TEST_SCHEMA).apply {
                put("id", 2L)
                put("name", null)
                put("score", 82.0)
                put("active", false)
            },
            GenericData.Record(TEST_SCHEMA).apply {
                put("id", 3L)
                put("name", "Charlie")
                put("score", 77.3)
                put("active", true)
            }
        )
    }

    /**
     * Writes an Avro file to a temp directory and returns the directory path.
     */
    fun writeAvroFile(dir: Path, filename: String, schema: Schema, records: List<GenericRecord>): Path {
        val filePath = dir.resolve(filename)
        val writer = GenericDatumWriter<GenericRecord>(schema)
        val dataFileWriter = DataFileWriter(writer)
        dataFileWriter.create(schema, filePath.toFile())
        for (record in records) {
            dataFileWriter.append(record)
        }
        dataFileWriter.close()
        return filePath
    }
}
