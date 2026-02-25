package io.qpointz.mill.source.format.excel

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.nio.file.Path

/**
 * Test utilities for creating in-memory Excel workbooks.
 */
object ExcelTestUtils {

    /**
     * Creates a test workbook with one sheet "Data" containing:
     * - Header: id, name, score, active
     * - Row 1: 1, Alice, 95.5, true
     * - Row 2: 2, (blank), 82.0, false
     * - Row 3: 3, Charlie, 77.3, true
     */
    fun createTestWorkbook(): Workbook {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Data")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("id")
        header.createCell(1).setCellValue("name")
        header.createCell(2).setCellValue("score")
        header.createCell(3).setCellValue("active")

        val row1 = sheet.createRow(1)
        row1.createCell(0).setCellValue(1.0)
        row1.createCell(1).setCellValue("Alice")
        row1.createCell(2).setCellValue(95.5)
        row1.createCell(3).setCellValue(true)

        val row2 = sheet.createRow(2)
        row2.createCell(0).setCellValue(2.0)
        row2.createCell(1).setCellValue("") // blank
        row2.createCell(2).setCellValue(82.0)
        row2.createCell(3).setCellValue(false)

        val row3 = sheet.createRow(3)
        row3.createCell(0).setCellValue(3.0)
        row3.createCell(1).setCellValue("Charlie")
        row3.createCell(2).setCellValue(77.3)
        row3.createCell(3).setCellValue(true)

        return workbook
    }

    /**
     * Creates a workbook with two sheets: "Sales" and "Expenses".
     */
    fun createMultiSheetWorkbook(): Workbook {
        val workbook = XSSFWorkbook()

        val sales = workbook.createSheet("Sales")
        val salesHeader = sales.createRow(0)
        salesHeader.createCell(0).setCellValue("item")
        salesHeader.createCell(1).setCellValue("amount")
        val salesRow = sales.createRow(1)
        salesRow.createCell(0).setCellValue("Widget")
        salesRow.createCell(1).setCellValue(100.0)

        val expenses = workbook.createSheet("Expenses")
        val expHeader = expenses.createRow(0)
        expHeader.createCell(0).setCellValue("item")
        expHeader.createCell(1).setCellValue("amount")
        val expRow = expenses.createRow(1)
        expRow.createCell(0).setCellValue("Rent")
        expRow.createCell(1).setCellValue(500.0)

        return workbook
    }

    /**
     * Creates a workbook with monthly sheets: "Jan", "Feb", "Mar", "Summary".
     * Each month sheet has header (month, item, amount) and 2 data rows.
     * "Summary" has a different structure (total, count).
     */
    fun createMonthlyWorkbook(): Workbook {
        val workbook = XSSFWorkbook()

        fun addMonthSheet(name: String, items: List<Triple<String, String, Double>>) {
            val sheet = workbook.createSheet(name)
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("month")
            header.createCell(1).setCellValue("item")
            header.createCell(2).setCellValue("amount")
            items.forEachIndexed { idx, (month, item, amount) ->
                val row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(month)
                row.createCell(1).setCellValue(item)
                row.createCell(2).setCellValue(amount)
            }
        }

        addMonthSheet("Jan", listOf(
            Triple("Jan", "Widget", 100.0),
            Triple("Jan", "Gadget", 200.0)
        ))
        addMonthSheet("Feb", listOf(
            Triple("Feb", "Widget", 150.0),
            Triple("Feb", "Gadget", 250.0)
        ))
        addMonthSheet("Mar", listOf(
            Triple("Mar", "Widget", 120.0),
            Triple("Mar", "Gadget", 180.0)
        ))

        // Summary sheet with different structure
        val summary = workbook.createSheet("Summary")
        val sHeader = summary.createRow(0)
        sHeader.createCell(0).setCellValue("total")
        sHeader.createCell(1).setCellValue("count")
        val sRow = summary.createRow(1)
        sRow.createCell(0).setCellValue(1000.0)
        sRow.createCell(1).setCellValue(6.0)

        return workbook
    }

    /**
     * Writes a workbook to a file in the given directory.
     */
    fun writeWorkbook(dir: Path, filename: String, workbook: Workbook): Path {
        val filePath = dir.resolve(filename)
        FileOutputStream(filePath.toFile()).use { fos ->
            workbook.write(fos)
        }
        return filePath
    }
}
