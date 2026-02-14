package io.qpointz.mill.source.format.text

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SettingsTest {

    // ==================== parseChar ====================

    @Test
    fun parseChar_shouldReturnNull_whenInputIsNull() {
        assertNull(parseChar(null))
    }

    @Test
    fun parseChar_shouldReturnNull_whenInputIsEmpty() {
        assertNull(parseChar(""))
    }

    @Test
    fun parseChar_shouldHandleTab() {
        assertEquals('\t', parseChar("\\t"))
    }

    @Test
    fun parseChar_shouldHandleNewline() {
        assertEquals('\n', parseChar("\\n"))
    }

    @Test
    fun parseChar_shouldHandleCarriageReturn() {
        assertEquals('\r', parseChar("\\r"))
    }

    @Test
    fun parseChar_shouldHandleBackslash() {
        assertEquals('\\', parseChar("\\\\"))
    }

    @Test
    fun parseChar_shouldReturnFirstChar() {
        assertEquals(',', parseChar(","))
        assertEquals('|', parseChar("|"))
    }

    // ==================== CsvSettings ====================

    @Test
    fun csvSettings_shouldCreateParserSettings_withDefaults() {
        val settings = CsvSettings()
        val ps = settings.toParserSettings()
        assertNotNull(ps)
        assertFalse(ps.isHeaderExtractionEnabled)
    }

    @Test
    fun csvSettings_shouldApplyDelimiterAndQuote() {
        val settings = CsvSettings(delimiter = '|', quote = '\'')
        val ps = settings.toParserSettings()
        assertEquals('|', ps.format.delimiter)
        assertEquals('\'', ps.format.quote)
    }

    @Test
    fun csvSettings_shouldApplyMaxColumns() {
        val settings = CsvSettings(maxColumns = 100)
        val ps = settings.toParserSettings()
        assertEquals(100, ps.maxColumns)
    }

    @Test
    fun csvSettings_shouldCreateWriterSettings() {
        val settings = CsvSettings(delimiter = ';')
        val ws = settings.toWriterSettings()
        assertEquals(';', ws.format.delimiter)
    }

    @Test
    fun csvSettings_shouldApplyUnescapedQuoteHandling() {
        val settings = CsvSettings(unescapedQuoteHandling = CsvUnescapedQuoteHandling.RAISE_ERROR)
        val ps = settings.toParserSettings()
        assertEquals(
            com.univocity.parsers.csv.UnescapedQuoteHandling.RAISE_ERROR,
            ps.unescapedQuoteHandling
        )
    }

    // ==================== CsvUnescapedQuoteHandling ====================

    @Test
    fun csvUnescapedQuoteHandling_shouldConvertAllValues() {
        for (v in CsvUnescapedQuoteHandling.entries) {
            assertNotNull(v.toUnivocity())
        }
    }

    // ==================== CsvFormatDescriptor ====================

    @Test
    fun csvDescriptor_shouldConvertToSettings() {
        val desc = CsvFormatDescriptor(
            delimiter = "|",
            hasHeader = false,
            skipEmptyLines = true
        )
        val settings = desc.toSettings()
        assertEquals('|', settings.delimiter)
        assertFalse(settings.hasHeader)
        assertEquals(true, settings.skipEmptyLines)
    }

    @Test
    fun csvDescriptor_shouldHandleTabDelimiter() {
        val desc = CsvFormatDescriptor(delimiter = "\\t")
        val settings = desc.toSettings()
        assertEquals('\t', settings.delimiter)
    }

    // ==================== FwfSettings ====================

    @Test
    fun fwfSettings_shouldCreateParserSettings() {
        val settings = FwfSettings(
            columns = listOf(FwfColumnDef("a", 0, 5), FwfColumnDef("b", 5, 10))
        )
        val ps = settings.toParserSettings()
        assertNotNull(ps)
        assertFalse(ps.isHeaderExtractionEnabled)
    }

    @Test
    fun fwfSettings_shouldCreateWriterSettings() {
        val settings = FwfSettings(
            columns = listOf(FwfColumnDef("a", 0, 5))
        )
        val ws = settings.toWriterSettings()
        assertNotNull(ws)
    }

    @Test
    fun fwfSettings_shouldApplyPadding() {
        val settings = FwfSettings(
            columns = listOf(FwfColumnDef("a", 0, 5)),
            padding = '_'
        )
        val ps = settings.toParserSettings()
        assertEquals('_', ps.format.padding)
    }

    @Test
    fun fwfColumnDef_shouldRejectInvalidRange() {
        assertThrows<IllegalArgumentException> {
            FwfColumnDef("bad", 5, 3) // end < start
        }
        assertThrows<IllegalArgumentException> {
            FwfColumnDef("bad", -1, 5) // negative start
        }
    }

    @Test
    fun fwfSettings_shouldRequireAtLeastOneColumn() {
        assertThrows<IllegalArgumentException> {
            FwfSettings(columns = emptyList())
        }
    }

    @Test
    fun fwfColumnDef_shouldCalculateWidth() {
        val col = FwfColumnDef("x", 0, 10)
        assertEquals(10, col.width)
    }

    // ==================== FwfFormatDescriptor ====================

    @Test
    fun fwfDescriptor_shouldConvertToSettings() {
        val desc = FwfFormatDescriptor(
            columns = listOf(
                FwfColumnDescriptor("id", 0, 5),
                FwfColumnDescriptor("name", 5, 20)
            ),
            hasHeader = true,
            keepPadding = true
        )
        val settings = desc.toSettings()
        assertEquals(2, settings.columns.size)
        assertEquals("id", settings.columns[0].name)
        assertEquals(0, settings.columns[0].start)
        assertEquals(5, settings.columns[0].end)
        assertTrue(settings.hasHeader)
        assertEquals(true, settings.keepPadding)
    }

    // ==================== TsvSettings ====================

    @Test
    fun tsvSettings_shouldCreateParserSettings_withDefaults() {
        val settings = TsvSettings()
        val ps = settings.toParserSettings()
        assertNotNull(ps)
        assertFalse(ps.isHeaderExtractionEnabled)
    }

    @Test
    fun tsvSettings_shouldApplyLineJoining() {
        val settings = TsvSettings(lineJoiningEnabled = true)
        val ps = settings.toParserSettings()
        assertTrue(ps.isLineJoiningEnabled)
    }

    @Test
    fun tsvSettings_shouldCreateWriterSettings() {
        val settings = TsvSettings()
        val ws = settings.toWriterSettings()
        assertNotNull(ws)
    }

    // ==================== TsvFormatDescriptor ====================

    @Test
    fun tsvDescriptor_shouldConvertToSettings() {
        val desc = TsvFormatDescriptor(
            hasHeader = false,
            lineJoiningEnabled = true,
            skipEmptyLines = true
        )
        val settings = desc.toSettings()
        assertFalse(settings.hasHeader)
        assertEquals(true, settings.lineJoiningEnabled)
        assertEquals(true, settings.skipEmptyLines)
    }
}
