package io.qpointz.mill.source.format.excel

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import org.apache.poi.ss.usermodel.Workbook

/**
 * Record source that reads multiple sheets from an Excel workbook.
 *
 * Uses a [SheetSelector] to determine which sheets to include, then
 * concatenates their rows into a single record stream. All selected
 * sheets must share the same schema (column structure).
 *
 * The workbook is **not** closed by this source — the caller manages its lifecycle.
 *
 * @property workbook     the Apache POI workbook
 * @property schema       the shared schema for all selected sheets
 * @property settings     sheet reading configuration
 * @property selector     sheet selection criteria
 */
class WorkbookRecordSource(
    private val workbook: Workbook,
    override val schema: RecordSchema,
    private val settings: SheetSettings = SheetSettings(),
    private val selector: SheetSelector = SheetSelector.ALL
) : FlowRecordSource {

    /**
     * Returns the list of sheets selected by the [selector].
     */
    fun selectedSheets() = selector.select(workbook)

    override fun iterator(): Iterator<Record> {
        val sheets = selectedSheets()
        return sheets.asSequence().flatMap { sheet ->
            val source = SheetRecordSource(sheet, schema, settings)
            source.asSequence()
        }.iterator()
    }
}
