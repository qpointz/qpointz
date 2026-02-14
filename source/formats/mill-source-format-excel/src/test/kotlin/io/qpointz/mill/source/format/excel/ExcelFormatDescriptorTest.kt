package io.qpointz.mill.source.format.excel

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExcelFormatDescriptorTest {

    @Test
    fun shouldSelectFirstSheet_byDefault() {
        val desc = ExcelFormatDescriptor()
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(1, sheets.size)
            assertEquals("Jan", sheets[0].sheetName)
        }
    }

    @Test
    fun shouldSelectBySheetName() {
        val desc = ExcelFormatDescriptor(sheetName = "Feb")
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(1, sheets.size)
            assertEquals("Feb", sheets[0].sheetName)
        }
    }

    @Test
    fun shouldSelectBySheetIndex() {
        val desc = ExcelFormatDescriptor(sheetIndex = 2)
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(1, sheets.size)
            assertEquals("Mar", sheets[0].sheetName)
        }
    }

    @Test
    fun shouldSelectAllSheets() {
        val desc = ExcelFormatDescriptor(allSheets = true)
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(4, sheets.size)
        }
    }

    @Test
    fun shouldSelectByPattern() {
        val desc = ExcelFormatDescriptor(sheetPattern = "(Jan|Feb|Mar)")
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(3, sheets.size)
            val names = sheets.map { s -> s.sheetName }.toSet()
            assertEquals(setOf("Jan", "Feb", "Mar"), names)
        }
    }

    @Test
    fun shouldExcludeByName() {
        val desc = ExcelFormatDescriptor(
            allSheets = true,
            excludeSheets = listOf("Summary")
        )
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(3, sheets.size)
            val names = sheets.map { s -> s.sheetName }.toSet()
            assertFalse(names.contains("Summary"))
        }
    }

    @Test
    fun shouldExcludeByPattern() {
        val desc = ExcelFormatDescriptor(
            allSheets = true,
            excludeSheetPattern = "Sum.*"
        )
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(3, sheets.size)
            val names = sheets.map { s -> s.sheetName }.toSet()
            assertFalse(names.contains("Summary"))
        }
    }

    @Test
    fun shouldCombinePatternWithExclude() {
        val desc = ExcelFormatDescriptor(
            sheetPattern = ".*",
            excludeSheets = listOf("Jan", "Summary")
        )
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(2, sheets.size)
            val names = sheets.map { s -> s.sheetName }.toSet()
            assertEquals(setOf("Feb", "Mar"), names)
        }
    }

    @Test
    fun shouldPrioritizeAllSheets_overPattern() {
        // allSheets takes priority even when sheetPattern is set
        val desc = ExcelFormatDescriptor(
            allSheets = true,
            sheetPattern = "Jan"
        )
        val selector = desc.toSheetSelector()

        val wb = ExcelTestUtils.createMonthlyWorkbook()
        wb.use {
            val sheets = selector.select(it)
            assertEquals(4, sheets.size) // allSheets wins
        }
    }

    @Test
    fun shouldCreateSheetSettings() {
        val desc = ExcelFormatDescriptor(hasHeader = false)
        val settings = desc.toSheetSettings()
        assertFalse(settings.hasHeader)
    }
}
