package io.qpointz.mill.source.format.excel

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class WorkbookRecordSourceTest {

    @Test
    fun shouldReadFromAllSheets() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val schema = ExcelSchemaInferer.infer(workbook.getSheetAt(0), SheetSettings())
        val source = WorkbookRecordSource(workbook, schema, SheetSettings(), SheetSelector.ALL)

        val result = source.toList()
        assertEquals(2, result.size) // 1 row from Sales + 1 row from Expenses
        workbook.close()
    }

    @Test
    fun shouldReadFromFirstSheetOnly() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val schema = ExcelSchemaInferer.infer(workbook.getSheetAt(0), SheetSettings())
        val source = WorkbookRecordSource(workbook, schema, SheetSettings(), SheetSelector.FIRST)

        val result = source.toList()
        assertEquals(1, result.size)
        assertEquals("Widget", result[0]["item"])
        workbook.close()
    }

    @Test
    fun shouldReturnSelectedSheets() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val schema = ExcelSchemaInferer.infer(workbook.getSheetAt(0), SheetSettings())
        val source = WorkbookRecordSource(workbook, schema, SheetSettings(), SheetSelector.ALL)

        val sheets = source.selectedSheets()
        assertEquals(2, sheets.size)
        workbook.close()
    }

    @Test
    fun shouldReadWithNameSelector() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val schema = ExcelSchemaInferer.infer(workbook.getSheetAt(1), SheetSettings())
        val selector = SheetSelector(include = listOf(SheetCriteria.ByName("Expenses")))
        val source = WorkbookRecordSource(workbook, schema, SheetSettings(), selector)

        val result = source.toList()
        assertEquals(1, result.size)
        assertEquals("Rent", result[0]["item"])
        workbook.close()
    }
}
