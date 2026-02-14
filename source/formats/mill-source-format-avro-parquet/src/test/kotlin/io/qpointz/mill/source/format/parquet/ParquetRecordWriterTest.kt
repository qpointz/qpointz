package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.Record
import io.qpointz.mill.source.format.avro.AvroSchemaConverter
import io.qpointz.mill.source.format.avro.ConstantSchemaSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ParquetRecordWriterTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun shouldWriteAndReadBackRecords() {
        val outputPath = tempDir.resolve("output.parquet")
        val settings = ParquetWriterSettings(ConstantSchemaSource(ParquetTestUtils.TEST_AVRO_SCHEMA))

        ParquetRecordWriter(settings, outputPath).use {
            it.open()
            it.write(Record.of("id" to 1L, "name" to "Alice", "score" to 95.5, "active" to true))
            it.write(Record.of("id" to 2L, "name" to null, "score" to 82.0, "active" to false))
            it.write(Record.of("id" to 3L, "name" to "Charlie", "score" to 77.3, "active" to true))
        }

        // Read back via ParquetRecordSource
        val schema = AvroSchemaConverter.convert(ParquetTestUtils.TEST_AVRO_SCHEMA)
        val source = ParquetRecordSource(outputPath.toUri(), schema)
        val records = source.toList()

        assertEquals(3, records.size)
        assertEquals(1L, records[0]["id"])
        assertEquals("Alice", records[0]["name"])
        assertEquals(95.5, records[0]["score"])
        assertEquals(true, records[0]["active"])

        assertEquals(2L, records[1]["id"])
        assertNull(records[1]["name"])

        assertEquals(3L, records[2]["id"])
        assertEquals("Charlie", records[2]["name"])
    }

    @Test
    fun shouldWriteEmptyFile() {
        val outputPath = tempDir.resolve("empty.parquet")
        val settings = ParquetWriterSettings(ConstantSchemaSource(ParquetTestUtils.TEST_AVRO_SCHEMA))

        ParquetRecordWriter(settings, outputPath).use {
            it.open()
        }

        val schema = AvroSchemaConverter.convert(ParquetTestUtils.TEST_AVRO_SCHEMA)
        val source = ParquetRecordSource(outputPath.toUri(), schema)
        assertTrue(source.toList().isEmpty())
    }

    @Test
    fun shouldThrow_whenWritingWithoutOpen() {
        val outputPath = tempDir.resolve("fail.parquet")
        val settings = ParquetWriterSettings(ConstantSchemaSource(ParquetTestUtils.TEST_AVRO_SCHEMA))
        val writer = ParquetRecordWriter(settings, outputPath)

        assertThrows(IllegalStateException::class.java) {
            writer.write(Record.of("id" to 1L))
        }
    }
}
