package io.qpointz.mill.source.format.excel

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SheetRecordSourceTest {

    @Test
    fun shouldReadAllRecords() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        val sheet = workbook.getSheetAt(0)
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings())
        val source = SheetRecordSource(sheet, schema)

        val result = source.toList()
        assertEquals(3, result.size)
        workbook.close()
    }

    @Test
    fun shouldMapFieldsCorrectly() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        val sheet = workbook.getSheetAt(0)
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings())
        val source = SheetRecordSource(sheet, schema)

        val result = source.toList()
        assertEquals(1L, result[0]["id"])
        assertEquals("Alice", result[0]["name"])
        assertEquals(95.5, result[0]["score"])
        assertEquals(true, result[0]["active"])
        workbook.close()
    }

    @Test
    fun shouldHandleBlankValues() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        val sheet = workbook.getSheetAt(0)
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings())
        val source = SheetRecordSource(sheet, schema)

        val result = source.toList()
        assertNull(result[1]["name"]) // blank treated as null
        workbook.close()
    }

    @Test
    fun shouldExposeCorrectSchema() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        val sheet = workbook.getSheetAt(0)
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings())
        val source = SheetRecordSource(sheet, schema)

        assertEquals(4, source.schema.size)
        assertEquals(listOf("id", "name", "score", "active"), source.schema.fieldNames)
        workbook.close()
    }

    @Test
    fun shouldHandleEmptySheet() {
        val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
        val sheet = workbook.createSheet("Empty")
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings())
        val source = SheetRecordSource(sheet, schema)

        val result = source.toList()
        assertTrue(result.isEmpty())
        workbook.close()
    }
}
