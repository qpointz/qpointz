package io.qpointz.mill.source.format.excel

import io.qpointz.mill.source.*
import org.apache.poi.ss.usermodel.WorkbookFactory

/**
 * [FormatHandler] for Excel files (.xlsx, .xls).
 *
 * Infers schema from the first selected sheet's header row and creates
 * record sources for reading data. When multiple sheets match the selector,
 * a [WorkbookRecordSource] concatenates their rows into a single stream.
 * Uses Apache POI for reading Excel files.
 *
 * @property settings  sheet reading configuration
 * @property selector  sheet selection criteria
 */
class ExcelFormatHandler(
    private val settings: SheetSettings = SheetSettings(),
    private val selector: SheetSelector = SheetSelector.FIRST
) : FormatHandler {

    /**
     * Infers the Mill [RecordSchema] from the first selected sheet.
     *
     * Opens the blob, creates a workbook, selects the first matching sheet,
     * and infers the schema from its header row or structure.
     */
    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema {
        val inputStream = blobSource.openInputStream(blob)
        return inputStream.use { stream ->
            val workbook = WorkbookFactory.create(stream)
            workbook.use { wb ->
                val sheets = selector.select(wb)
                if (sheets.isEmpty()) return@use RecordSchema.empty()
                ExcelSchemaInferer.infer(sheets[0], settings)
            }
        }
    }

    /**
     * Creates a record source for the given blob.
     *
     * Opens the workbook and returns a [WorkbookRecordSource] that reads
     * all selected sheets, concatenating their rows into a single stream.
     * All selected sheets must share the same column structure as [schema].
     */
    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        val inputStream = blobSource.openInputStream(blob)
        val workbook = WorkbookFactory.create(inputStream)
        val sheets = selector.select(workbook)
        if (sheets.isEmpty()) {
            workbook.close()
            return InMemoryRecordSource.empty(schema)
        }
        return WorkbookRecordSource(workbook, schema, settings, selector)
    }
}
