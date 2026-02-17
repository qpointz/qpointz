package io.qpointz.mill.source.format.excel

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExcelSchemaInfererTest {

    @Test
    fun shouldInferFromHeaderRow() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        val sheet = workbook.getSheetAt(0)
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings(hasHeader = true))

        assertEquals(4, schema.size)
        assertEquals("id", schema.fields[0].name)
        assertEquals("name", schema.fields[1].name)
        assertEquals("score", schema.fields[2].name)
        assertEquals("active", schema.fields[3].name)
        workbook.close()
    }

    @Test
    fun shouldGenerateColumnNames_whenNoHeader() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        val sheet = workbook.getSheetAt(0)
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings(hasHeader = false))

        assertEquals(4, schema.size)
        assertEquals("col_0", schema.fields[0].name)
        assertEquals("col_1", schema.fields[1].name)
        workbook.close()
    }

    @Test
    fun shouldUseExplicitColumnDefs() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        val sheet = workbook.getSheetAt(0)
        val columns = listOf(
            SheetColumnDef("first_col", 0),
            SheetColumnDef("second_col", 2)
        )
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings(columns = columns))

        assertEquals(2, schema.size)
        assertEquals("first_col", schema.fields[0].name)
        assertEquals("second_col", schema.fields[1].name)
        assertEquals(0, schema.fields[0].index)
        assertEquals(2, schema.fields[1].index)
        workbook.close()
    }

    @Test
    fun shouldHandleEmptySheet() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Empty")
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings())

        assertEquals(0, schema.size)
        workbook.close()
    }

    @Test
    fun shouldHandleEmptySheet_noHeader() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Empty")
        val schema = ExcelSchemaInferer.infer(sheet, SheetSettings(hasHeader = false))

        assertEquals(0, schema.size)
        workbook.close()
    }
}
