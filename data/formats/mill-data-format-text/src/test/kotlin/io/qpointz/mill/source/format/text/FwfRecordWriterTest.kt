package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class FwfRecordWriterTest {

    private val columns = listOf(
        FwfColumnDef("id", 0, 5),
        FwfColumnDef("name", 5, 20),
        FwfColumnDef("score", 20, 30)
    )

    private val settings = FwfSettings(columns = columns)

    private val schema = RecordSchema.of(
        "id" to DatabaseType.string(true, -1),
        "name" to DatabaseType.string(true, -1),
        "score" to DatabaseType.string(true, -1)
    )

    @Test
    fun shouldWriteFixedWidthRecords() {
        val baos = ByteArrayOutputStream()
        val writer = FwfRecordWriter(schema, settings, baos)
        writer.use {
            it.open()
            it.write(Record.of("id" to "1", "name" to "Alice", "score" to "95.5"))
            it.write(Record.of("id" to "2", "name" to "Bob", "score" to "82.0"))
        }

        val output = baos.toString(Charsets.UTF_8)
        val lines = output.trim().lines()
        assertEquals(2, lines.size)
        // Each line should be 30 chars (total width)
        assertEquals(30, lines[0].length)
        assertTrue(lines[0].startsWith("1"))
        assertEquals("Alice", lines[0].substring(5, 20).trim())
    }

    @Test
    fun shouldWriteHeader_whenConfigured() {
        val headerSettings = settings.copy(hasHeader = true)
        val baos = ByteArrayOutputStream()
        val writer = FwfRecordWriter(schema, headerSettings, baos)
        writer.use {
            it.open()
            it.write(Record.of("id" to "1", "name" to "Alice", "score" to "95.5"))
        }

        val output = baos.toString(Charsets.UTF_8)
        val lines = output.trim().lines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].contains("id"))
        assertTrue(lines[0].contains("name"))
    }

    @Test
    fun shouldHandleNullValues() {
        val baos = ByteArrayOutputStream()
        val writer = FwfRecordWriter(schema, settings, baos)
        writer.use {
            it.open()
            it.write(Record.of("id" to "1", "name" to null, "score" to "95.5"))
        }

        val output = baos.toString(Charsets.UTF_8)
        val lines = output.trim().lines()
        assertEquals(1, lines.size)
        // Null name should be padded with spaces
        assertTrue(lines[0].substring(5, 20).isBlank())
    }

    @Test
    fun shouldRoundTrip() {
        // Write
        val baos = ByteArrayOutputStream()
        val writer = FwfRecordWriter(schema, settings, baos)
        writer.use {
            it.open()
            it.write(Record.of("id" to "1", "name" to "Alice", "score" to "95.5"))
            it.write(Record.of("id" to "2", "name" to "Bob", "score" to "82.0"))
        }

        // Read back
        val fwfContent = baos.toString(Charsets.UTF_8)
        val source = FwfRecordSource(
            CsvTestUtils.toInputStream(fwfContent),
            schema,
            settings
        )
        val result = source.toList()

        assertEquals(2, result.size)
        assertEquals("1", result[0]["id"]?.toString()?.trim())
        assertEquals("Alice", result[0]["name"]?.toString()?.trim())
        assertEquals("95.5", result[0]["score"]?.toString()?.trim())
        assertEquals("Bob", result[1]["name"]?.toString()?.trim())
    }
}
