package io.qpointz.mill.source.format.avro

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AvroRecordSourceTest {

    @Test
    fun shouldReadAllRecords() {
        val records = AvroTestUtils.createTestRecords()
        val inputStream = AvroTestUtils.createAvroInputStream(AvroTestUtils.TEST_SCHEMA, records)
        val schema = AvroSchemaConverter.convert(AvroTestUtils.TEST_SCHEMA)

        val source = AvroRecordSource(inputStream, schema)
        val result = source.toList()

        assertEquals(3, result.size)
    }

    @Test
    fun shouldMapFieldsCorrectly() {
        val records = AvroTestUtils.createTestRecords()
        val inputStream = AvroTestUtils.createAvroInputStream(AvroTestUtils.TEST_SCHEMA, records)
        val schema = AvroSchemaConverter.convert(AvroTestUtils.TEST_SCHEMA)

        val source = AvroRecordSource(inputStream, schema)
        val result = source.toList()

        val first = result[0]
        assertEquals(1L, first["id"])
        assertEquals("Alice", first["name"])
        assertEquals(95.5, first["score"])
        assertEquals(true, first["active"])
    }

    @Test
    fun shouldHandleNullValues() {
        val records = AvroTestUtils.createTestRecords()
        val inputStream = AvroTestUtils.createAvroInputStream(AvroTestUtils.TEST_SCHEMA, records)
        val schema = AvroSchemaConverter.convert(AvroTestUtils.TEST_SCHEMA)

        val source = AvroRecordSource(inputStream, schema)
        val result = source.toList()

        val second = result[1]
        assertEquals(2L, second["id"])
        assertNull(second["name"])
        assertEquals(82.0, second["score"])
        assertEquals(false, second["active"])
    }

    @Test
    fun shouldExposeCorrectSchema() {
        val records = AvroTestUtils.createTestRecords()
        val inputStream = AvroTestUtils.createAvroInputStream(AvroTestUtils.TEST_SCHEMA, records)
        val schema = AvroSchemaConverter.convert(AvroTestUtils.TEST_SCHEMA)

        val source = AvroRecordSource(inputStream, schema)
        assertEquals(4, source.schema.size)
        assertEquals(listOf("id", "name", "score", "active"), source.schema.fieldNames)
    }

    @Test
    fun shouldHandleEmptyFile() {
        val inputStream = AvroTestUtils.createAvroInputStream(AvroTestUtils.TEST_SCHEMA, emptyList())
        val schema = AvroSchemaConverter.convert(AvroTestUtils.TEST_SCHEMA)

        val source = AvroRecordSource(inputStream, schema)
        val result = source.toList()

        assertTrue(result.isEmpty())
    }
}
