package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TsvRecordSourceTest {

    private fun stringSchema(vararg names: String): RecordSchema =
        RecordSchema.of(*names.map { it to DatabaseType.string(true, -1) }.toTypedArray())

    private val tsvContent = "id\tname\tscore\n1\tAlice\t95.5\n2\t\t82.0\n3\tCharlie\t77.3"

    @Test
    fun shouldReadAllRecords() {
        val schema = stringSchema("id", "name", "score")
        val source = TsvRecordSource(
            CsvTestUtils.toInputStream(tsvContent),
            schema
        )
        val result = source.toList()
        assertEquals(3, result.size)
    }

    @Test
    fun shouldMapFieldsCorrectly() {
        val schema = stringSchema("id", "name", "score")
        val source = TsvRecordSource(
            CsvTestUtils.toInputStream(tsvContent),
            schema
        )
        val result = source.toList()
        assertEquals("1", result[0]["id"])
        assertEquals("Alice", result[0]["name"])
        assertEquals("95.5", result[0]["score"])
    }

    @Test
    fun shouldHandleEmptyValues() {
        val schema = stringSchema("id", "name", "score")
        val source = TsvRecordSource(
            CsvTestUtils.toInputStream(tsvContent),
            schema
        )
        val result = source.toList()
        // Empty field between tabs
        assertTrue(result[1]["name"] == null || result[1]["name"].toString().isEmpty())
    }

    @Test
    fun shouldHandleNoHeader() {
        val noHeaderContent = "1\tAlice\t95.5\n2\tBob\t82.0"
        val schema = stringSchema("col_0", "col_1", "col_2")
        val settings = TsvSettings(hasHeader = false)
        val source = TsvRecordSource(
            CsvTestUtils.toInputStream(noHeaderContent),
            schema,
            settings
        )
        val result = source.toList()
        assertEquals(2, result.size)
        assertEquals("1", result[0]["col_0"])
        assertEquals("Alice", result[0]["col_1"])
    }

    @Test
    fun shouldHandleEmptyFile() {
        val schema = RecordSchema.empty()
        val source = TsvRecordSource(
            CsvTestUtils.toInputStream(""),
            schema,
            TsvSettings(hasHeader = false)
        )
        val result = source.toList()
        assertTrue(result.isEmpty())
    }

    @Test
    fun shouldExposeCorrectSchema() {
        val schema = stringSchema("id", "name", "score")
        val source = TsvRecordSource(
            CsvTestUtils.toInputStream(tsvContent),
            schema
        )
        assertEquals(3, source.schema.size)
        assertEquals(listOf("id", "name", "score"), source.schema.fieldNames)
    }
}
