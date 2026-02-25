package io.qpointz.mill.source.format.avro

import io.qpointz.mill.source.Record
import org.apache.avro.file.DataFileStream
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class AvroRecordWriterTest {

    @Test
    fun shouldWriteRecordsToAvroFormat() {
        val baos = ByteArrayOutputStream()
        val settings = AvroWriterSettings(ConstantSchemaSource(AvroTestUtils.TEST_SCHEMA))
        val writer = AvroRecordWriter(settings, baos)

        writer.use {
            it.open()
            it.write(Record.of("id" to 1L, "name" to "Alice", "score" to 95.5, "active" to true))
            it.write(Record.of("id" to 2L, "name" to null, "score" to 82.0, "active" to false))
        }

        // Read back and verify
        val records = readBackAvro(baos.toByteArray())

        assertEquals(2, records.size)
        assertEquals(1L, records[0].get("id"))
        assertEquals("Alice", records[0].get("name").toString())
        assertEquals(95.5, records[0].get("score"))
        assertEquals(true, records[0].get("active"))

        assertEquals(2L, records[1].get("id"))
        assertNull(records[1].get("name"))
        assertEquals(82.0, records[1].get("score"))
        assertEquals(false, records[1].get("active"))
    }

    @Test
    fun shouldRoundTrip_writeAndReadBack() {
        val baos = ByteArrayOutputStream()
        val settings = AvroWriterSettings(ConstantSchemaSource(AvroTestUtils.TEST_SCHEMA))
        val schema = AvroSchemaConverter.convert(AvroTestUtils.TEST_SCHEMA)

        // Write
        AvroRecordWriter(settings, baos).use {
            it.open()
            it.write(Record.of("id" to 10L, "name" to "Test", "score" to 99.9, "active" to true))
            it.write(Record.of("id" to 20L, "name" to "Other", "score" to 50.0, "active" to false))
        }

        // Read back via AvroRecordSource
        val source = AvroRecordSource(ByteArrayInputStream(baos.toByteArray()), schema)
        val records = source.toList()

        assertEquals(2, records.size)
        assertEquals(10L, records[0]["id"])
        assertEquals("Test", records[0]["name"])
        assertEquals(99.9, records[0]["score"])
        assertEquals(true, records[0]["active"])
        assertEquals(20L, records[1]["id"])
    }

    @Test
    fun shouldWriteEmptyFile() {
        val baos = ByteArrayOutputStream()
        val settings = AvroWriterSettings(ConstantSchemaSource(AvroTestUtils.TEST_SCHEMA))

        AvroRecordWriter(settings, baos).use {
            it.open()
        }

        val records = readBackAvro(baos.toByteArray())
        assertTrue(records.isEmpty())
    }

    @Test
    fun shouldThrow_whenWritingWithoutOpen() {
        val baos = ByteArrayOutputStream()
        val settings = AvroWriterSettings(ConstantSchemaSource(AvroTestUtils.TEST_SCHEMA))
        val writer = AvroRecordWriter(settings, baos)

        assertThrows(IllegalStateException::class.java) {
            writer.write(Record.of("id" to 1L))
        }
    }
}

private fun readBackAvro(bytes: ByteArray): List<GenericRecord> {
    val reader = GenericDatumReader<GenericRecord>()
    val dataFileStream = DataFileStream(ByteArrayInputStream(bytes), reader)
    val result = mutableListOf<GenericRecord>()
    while (dataFileStream.hasNext()) {
        result.add(dataFileStream.next())
    }
    dataFileStream.close()
    return result
}

class AvroSchemaSourceTest {

    @Test
    fun shouldReturnConstantSchema() {
        val source = ConstantSchemaSource(AvroTestUtils.TEST_SCHEMA)
        assertEquals(AvroTestUtils.TEST_SCHEMA, source.schema())
    }

    @Test
    fun shouldParseJsonSchema() {
        val json = AvroTestUtils.TEST_SCHEMA.toString()
        val source = JsonSchemaSource(json)
        val parsed = source.schema()
        assertEquals(AvroTestUtils.TEST_SCHEMA.fields.size, parsed.fields.size)
        assertEquals("id", parsed.fields[0].name())
    }
}
