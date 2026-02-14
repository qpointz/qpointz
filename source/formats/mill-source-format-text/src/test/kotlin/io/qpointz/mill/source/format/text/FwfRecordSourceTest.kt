package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FwfRecordSourceTest {

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

    private val fwfContent = "1    Alice          95.5      \n2    Bob            82.0      \n3    Charlie        77.3      "

    @Test
    fun shouldReadAllRecords() {
        val source = FwfRecordSource(
            CsvTestUtils.toInputStream(fwfContent),
            schema,
            settings
        )
        val result = source.toList()
        assertEquals(3, result.size)
    }

    @Test
    fun shouldMapFieldsCorrectly() {
        val source = FwfRecordSource(
            CsvTestUtils.toInputStream(fwfContent),
            schema,
            settings
        )
        val result = source.toList()
        assertEquals("1", result[0]["id"]?.toString()?.trim())
        assertEquals("Alice", result[0]["name"]?.toString()?.trim())
        assertEquals("95.5", result[0]["score"]?.toString()?.trim())
    }

    @Test
    fun shouldTrimValues_whenWhitespaceIgnored() {
        val trimSettings = settings.copy(
            ignoreLeadingWhitespaces = true,
            ignoreTrailingWhitespaces = true
        )
        val source = FwfRecordSource(
            CsvTestUtils.toInputStream(fwfContent),
            schema,
            trimSettings
        )
        val result = source.toList()
        assertEquals("Alice", result[0]["name"])
        assertFalse(result[0]["name"].toString().endsWith(" "))
    }

    @Test
    fun shouldKeepPadding_whenConfigured() {
        val padSettings = settings.copy(keepPadding = true)
        val source = FwfRecordSource(
            CsvTestUtils.toInputStream(fwfContent),
            schema,
            padSettings
        )
        val result = source.toList()
        // With keepPadding the value retains its full width
        assertEquals(15, result[0]["name"].toString().length)
    }

    @Test
    fun shouldSkipHeader_whenConfigured() {
        val withHeader = "ID   NAME           SCORE     \n" + fwfContent
        val headerSettings = settings.copy(hasHeader = true)
        val source = FwfRecordSource(
            CsvTestUtils.toInputStream(withHeader),
            schema,
            headerSettings
        )
        val result = source.toList()
        assertEquals(3, result.size)
        assertEquals("1", result[0]["id"]?.toString()?.trim())
    }

    @Test
    fun shouldHandleEmptyFile() {
        val source = FwfRecordSource(
            CsvTestUtils.toInputStream(""),
            schema,
            settings
        )
        val result = source.toList()
        assertTrue(result.isEmpty())
    }
}
