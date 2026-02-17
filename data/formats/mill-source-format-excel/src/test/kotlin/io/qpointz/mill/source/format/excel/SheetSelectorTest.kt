package io.qpointz.mill.source.format.excel

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SheetSelectorTest {

    @Test
    fun shouldSelectAllSheets_byDefault() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val result = SheetSelector.ALL.select(workbook)

        assertEquals(2, result.size)
        workbook.close()
    }

    @Test
    fun shouldSelectFirstSheet() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val result = SheetSelector.FIRST.select(workbook)

        assertEquals(1, result.size)
        assertEquals("Sales", result[0].sheetName)
        workbook.close()
    }

    @Test
    fun shouldSelectByName() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val selector = SheetSelector(include = listOf(SheetCriteria.ByName("Expenses")))
        val result = selector.select(workbook)

        assertEquals(1, result.size)
        assertEquals("Expenses", result[0].sheetName)
        workbook.close()
    }

    @Test
    fun shouldSelectByNameCaseInsensitive() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val selector = SheetSelector(include = listOf(SheetCriteria.ByName("sales")))
        val result = selector.select(workbook)

        assertEquals(1, result.size)
        assertEquals("Sales", result[0].sheetName)
        workbook.close()
    }

    @Test
    fun shouldSelectByIndex() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val selector = SheetSelector(include = listOf(SheetCriteria.ByIndex(1)))
        val result = selector.select(workbook)

        assertEquals(1, result.size)
        assertEquals("Expenses", result[0].sheetName)
        workbook.close()
    }

    @Test
    fun shouldSelectByPattern() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val selector = SheetSelector(include = listOf(SheetCriteria.ByPattern("S.*")))
        val result = selector.select(workbook)

        assertEquals(1, result.size)
        assertEquals("Sales", result[0].sheetName)
        workbook.close()
    }

    @Test
    fun shouldExcludeSheets() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val selector = SheetSelector(
            exclude = listOf(SheetCriteria.ByName("Sales"))
        )
        val result = selector.select(workbook)

        assertEquals(1, result.size)
        assertEquals("Expenses", result[0].sheetName)
        workbook.close()
    }

    @Test
    fun shouldHandleIncludeAndExclude() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        val selector = SheetSelector(
            include = listOf(SheetCriteria.AnySheet),
            exclude = listOf(SheetCriteria.ByIndex(0))
        )
        val result = selector.select(workbook)

        assertEquals(1, result.size)
        assertEquals("Expenses", result[0].sheetName)
        workbook.close()
    }
}
