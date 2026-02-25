package io.qpointz.mill.source.format.excel

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.LocalBlobSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ExcelFormatHandlerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun shouldInferSchemaFromExcelFile() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        ExcelTestUtils.writeWorkbook(tempDir, "test.xlsx", workbook)
        workbook.close()

        val handler = ExcelFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()
        assertEquals(1, blobs.size)

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(4, schema.size)
        assertEquals("id", schema.fields[0].name)
        assertEquals("name", schema.fields[1].name)
        assertEquals("score", schema.fields[2].name)
        assertEquals("active", schema.fields[3].name)
    }

    @Test
    fun shouldCreateRecordSourceFromExcelFile() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        ExcelTestUtils.writeWorkbook(tempDir, "test.xlsx", workbook)
        workbook.close()

        val handler = ExcelFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        val source = handler.createRecordSource(blobs[0], blobSource, schema)

        assertTrue(source is FlowRecordSource)
        val result = (source as FlowRecordSource).toList()

        assertEquals(3, result.size)
        assertEquals(1L, result[0]["id"])
        assertEquals("Alice", result[0]["name"])
        assertNull(result[1]["name"])
    }

    @Test
    fun shouldReadSpecificSheet() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        ExcelTestUtils.writeWorkbook(tempDir, "multi.xlsx", workbook)
        workbook.close()

        val handler = ExcelFormatHandler(
            selector = SheetSelector(include = listOf(SheetCriteria.ByName("Expenses")))
        )
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals("item", schema.fields[0].name)

        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        val result = source.toList()
        assertEquals(1, result.size)
        assertEquals("Rent", result[0]["item"])
    }

    @Test
    fun shouldConcatenateMultipleSheets_allSheets() {
        val workbook = ExcelTestUtils.createMultiSheetWorkbook()
        ExcelTestUtils.writeWorkbook(tempDir, "multi.xlsx", workbook)
        workbook.close()

        val handler = ExcelFormatHandler(
            selector = SheetSelector(include = listOf(SheetCriteria.AnySheet))
        )
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        val result = source.toList()

        // Sales has 1 data row + Expenses has 1 data row = 2 total
        assertEquals(2, result.size)
        val items = result.map { it["item"] }.toSet()
        assertTrue(items.contains("Widget"))
        assertTrue(items.contains("Rent"))
    }

    @Test
    fun shouldConcatenateMonthlySheets_byPattern() {
        val workbook = ExcelTestUtils.createMonthlyWorkbook()
        ExcelTestUtils.writeWorkbook(tempDir, "monthly.xlsx", workbook)
        workbook.close()

        // Select only the 3 month sheets, exclude Summary
        val handler = ExcelFormatHandler(
            selector = SheetSelector(
                include = listOf(SheetCriteria.ByPattern("(Jan|Feb|Mar)")),
                exclude = emptyList()
            )
        )
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(3, schema.size)
        assertEquals("month", schema.fields[0].name)
        assertEquals("item", schema.fields[1].name)
        assertEquals("amount", schema.fields[2].name)

        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        val result = source.toList()

        // 3 sheets x 2 rows = 6 rows total
        assertEquals(6, result.size)
        val months = result.map { it["month"] }.toSet()
        assertEquals(setOf("Jan", "Feb", "Mar"), months)
    }

    @Test
    fun shouldConcatenateMonthlySheets_allExcludeSummary() {
        val workbook = ExcelTestUtils.createMonthlyWorkbook()
        ExcelTestUtils.writeWorkbook(tempDir, "monthly.xlsx", workbook)
        workbook.close()

        val handler = ExcelFormatHandler(
            selector = SheetSelector(
                include = listOf(SheetCriteria.AnySheet),
                exclude = listOf(SheetCriteria.ByName("Summary"))
            )
        )
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        val result = source.toList()

        assertEquals(6, result.size)
    }

    @Test
    fun shouldReturnEmpty_whenNoSheetsMatch() {
        val workbook = ExcelTestUtils.createTestWorkbook()
        ExcelTestUtils.writeWorkbook(tempDir, "test.xlsx", workbook)
        workbook.close()

        val handler = ExcelFormatHandler(
            selector = SheetSelector(include = listOf(SheetCriteria.ByName("NonExistent")))
        )
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertTrue(schema.fields.isEmpty())
    }
}
