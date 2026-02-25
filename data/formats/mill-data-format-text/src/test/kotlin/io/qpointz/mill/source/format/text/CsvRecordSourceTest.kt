package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CsvRecordSourceTest {

    private fun stringSchema(vararg names: String): RecordSchema =
        RecordSchema.of(*names.map { it to DatabaseType.string(true, -1) }.toTypedArray())

    @Test
    fun shouldReadAllRecords() {
        val schema = stringSchema("id", "name", "score", "active")
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(CsvTestUtils.SIMPLE_CSV),
            schema
        )
        val result = source.toList()
        assertEquals(3, result.size)
    }

    @Test
    fun shouldMapFieldsCorrectly() {
        val schema = stringSchema("id", "name", "score", "active")
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(CsvTestUtils.SIMPLE_CSV),
            schema
        )
        val result = source.toList()
        val first = result[0]
        assertEquals("1", first["id"])
        assertEquals("Alice", first["name"])
        assertEquals("95.5", first["score"])
        assertEquals("true", first["active"])
    }

    @Test
    fun shouldHandleNullValues() {
        val schema = stringSchema("id", "name", "score", "active")
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(CsvTestUtils.SIMPLE_CSV),
            schema
        )
        val result = source.toList()
        val second = result[1]
        assertEquals("2", second["id"])
        assertNull(second["name"])
    }

    @Test
    fun shouldHandleTabDelimiter() {
        val schema = stringSchema("id", "name", "score")
        val settings = CsvSettings(delimiter = '\t')
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(CsvTestUtils.SIMPLE_TSV),
            schema,
            settings
        )
        val result = source.toList()
        assertEquals(3, result.size)
        assertEquals("Alice", result[0]["name"])
    }

    @Test
    fun shouldHandleNoHeader() {
        val schema = stringSchema("col_0", "col_1", "col_2", "col_3")
        val settings = CsvSettings(hasHeader = false)
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(CsvTestUtils.NO_HEADER_CSV),
            schema,
            settings
        )
        val result = source.toList()
        assertEquals(3, result.size)
        assertEquals("1", result[0]["col_0"])
        assertEquals("Alice", result[0]["col_1"])
    }

    @Test
    fun shouldHandleQuotedFields() {
        val schema = stringSchema("id", "name", "description")
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(CsvTestUtils.QUOTED_CSV),
            schema
        )
        val result = source.toList()
        assertEquals(3, result.size)
        assertEquals("Has a, comma", result[0]["description"])
        assertEquals("Quoted \"word\"", result[1]["description"])
    }

    @Test
    fun shouldHandleEmptyFile() {
        val schema = RecordSchema.empty()
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(""),
            schema,
            CsvSettings(hasHeader = false)
        )
        val result = source.toList()
        assertTrue(result.isEmpty())
    }

    @Test
    fun shouldExposeCorrectSchema() {
        val schema = stringSchema("id", "name", "score", "active")
        val source = CsvRecordSource(
            CsvTestUtils.toInputStream(CsvTestUtils.SIMPLE_CSV),
            schema
        )
        assertEquals(4, source.schema.size)
        assertEquals(listOf("id", "name", "score", "active"), source.schema.fieldNames)
    }
}
