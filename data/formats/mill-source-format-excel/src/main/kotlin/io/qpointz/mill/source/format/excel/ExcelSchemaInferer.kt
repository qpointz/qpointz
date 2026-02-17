package io.qpointz.mill.source.format.excel

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SchemaField
import io.qpointz.mill.types.sql.DatabaseType
import org.apache.poi.ss.usermodel.Sheet

/**
 * Infers a Mill [RecordSchema] from an Excel sheet.
 *
 * If the sheet has a header row, column names are taken from it.
 * Otherwise, names are generated as `col_0`, `col_1`, etc.
 * All columns are typed as nullable [DatabaseType.string] since
 * Excel cells can contain mixed types.
 */
object ExcelSchemaInferer {

    /**
     * Infers the schema from the given [sheet] using the provided [settings].
     */
    fun infer(sheet: Sheet, settings: SheetSettings): RecordSchema {
        if (settings.columns.isNotEmpty()) {
            return fromColumnDefs(settings.columns)
        }

        if (settings.hasHeader && sheet.lastRowNum >= 0) {
            return fromHeaderRow(sheet)
        }

        // No header, no explicit columns — scan first data row for column count
        val firstRow = sheet.getRow(settings.effectiveStartRow)
        if (firstRow == null) return RecordSchema.empty()

        val colCount = firstRow.lastCellNum.toInt()
        val fields = (0 until colCount).map { idx ->
            SchemaField("col_$idx", idx, DatabaseType.string(true, -1))
        }
        return RecordSchema(fields)
    }

    private fun fromColumnDefs(columns: List<SheetColumnDef>): RecordSchema {
        val fields = columns.mapIndexed { idx, col ->
            SchemaField(col.name, col.index, DatabaseType.string(true, -1))
        }
        return RecordSchema(fields)
    }

    private fun fromHeaderRow(sheet: Sheet): RecordSchema {
        val headerRow = sheet.getRow(0) ?: return RecordSchema.empty()
        val colCount = headerRow.lastCellNum.toInt()
        val fields = (0 until colCount).map { idx ->
            val cell = headerRow.getCell(idx)
            val name = cell?.stringCellValue?.trim() ?: "col_$idx"
            SchemaField(name, idx, DatabaseType.string(true, -1))
        }
        return RecordSchema(fields)
    }
}
