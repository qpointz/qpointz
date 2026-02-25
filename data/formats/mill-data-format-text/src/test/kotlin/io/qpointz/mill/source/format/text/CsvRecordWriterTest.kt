package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class CsvRecordWriterTest {

    private val schema = RecordSchema.of(
        "id" to DatabaseType.string(true, -1),
        "name" to DatabaseType.string(true, -1),
        "score" to DatabaseType.string(true, -1)
    )

    @Test
    fun shouldWriteHeaderAndRecords() {
        val baos = ByteArrayOutputStream()
        val writer = CsvRecordWriter(schema, CsvSettings(), baos)
        writer.use {
            it.open()
            it.write(Record.of("id" to "1", "name" to "Alice", "score" to "95.5"))
            it.write(Record.of("id" to "2", "name" to "Bob", "score" to "82.0"))
        }

        val output = baos.toString(Charsets.UTF_8)
        val lines = output.trim().lines()
        assertEquals(3, lines.size)
        assertTrue(lines[0].contains("id"))
        assertTrue(lines[0].contains("name"))
        assertTrue(lines[1].contains("Alice"))
        assertTrue(lines[2].contains("Bob"))
    }

    @Test
    fun shouldWriteWithoutHeader() {
        val baos = ByteArrayOutputStream()
        val writer = CsvRecordWriter(schema, CsvSettings(hasHeader = false), baos)
        writer.use {
            it.open()
            it.write(Record.of("id" to "1", "name" to "Alice", "score" to "95.5"))
        }

        val output = baos.toString(Charsets.UTF_8)
        val lines = output.trim().lines()
        assertEquals(1, lines.size)
        assertTrue(lines[0].contains("Alice"))
    }

    @Test
    fun shouldHandleNullValues() {
        val baos = ByteArrayOutputStream()
        val writer = CsvRecordWriter(schema, CsvSettings(), baos)
        writer.use {
            it.open()
            it.write(Record.of("id" to "1", "name" to null, "score" to "95.5"))
        }

        val output = baos.toString(Charsets.UTF_8)
        assertTrue(output.contains("1,"))  // null is represented as empty
    }

    @Test
    fun shouldRoundTrip() {
        // Write
        val baos = ByteArrayOutputStream()
        val writer = CsvRecordWriter(schema, CsvSettings(), baos)
        writer.use {
            it.open()
            it.write(Record.of("id" to "1", "name" to "Alice", "score" to "95.5"))
            it.write(Record.of("id" to "2", "name" to "Bob", "score" to "82.0"))
        }

        // Read back
        val csvContent = baos.toString(Charsets.UTF_8)
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(csvContent),
            schema
        )
        val result = source.toList()

        assertEquals(2, result.size)
        assertEquals("Alice", result[0]["name"])
        assertEquals("Bob", result[1]["name"])
    }
}
