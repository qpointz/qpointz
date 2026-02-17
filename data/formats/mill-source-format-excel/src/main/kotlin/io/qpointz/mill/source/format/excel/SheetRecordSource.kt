package io.qpointz.mill.source.format.excel

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import org.apache.poi.ss.usermodel.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Row-oriented record source that reads a single Excel sheet.
 *
 * Reads the given [sheet] row-by-row starting from [SheetSettings.effectiveStartRow],
 * converting each row to a Mill [Record]. Cell values are extracted as their
 * natural types (strings, numbers, booleans, dates).
 *
 * @property sheet    the Apache POI sheet to read
 * @property schema   the Mill schema describing the expected fields
 * @property settings sheet reading configuration
 */
class SheetRecordSource(
    private val sheet: Sheet,
    override val schema: RecordSchema,
    private val settings: SheetSettings = SheetSettings()
) : FlowRecordSource {

    private val formulaEvaluator: FormulaEvaluator? by lazy {
        if (settings.evaluateFormulas) {
            sheet.workbook.creationHelper.createFormulaEvaluator()
        } else null
    }

    override fun iterator(): Iterator<Record> {
        val startRow = settings.effectiveStartRow
        val lastRow = sheet.lastRowNum

        return object : Iterator<Record> {
            private var currentRow = startRow

            override fun hasNext(): Boolean = currentRow <= lastRow

            override fun next(): Record {
                if (currentRow > lastRow) throw NoSuchElementException()
                val row = sheet.getRow(currentRow)
                currentRow++
                return toRecord(row)
            }
        }
    }

    private fun toRecord(row: Row?): Record {
        if (row == null) {
            return Record(schema.fields.associate { it.name to null })
        }
        val values = schema.fields.associate { field ->
            val cell = row.getCell(field.index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
            field.name to extractCellValue(cell)
        }
        return Record(values)
    }

    /**
     * Extracts the value from a POI [Cell], returning a Mill-compatible type.
     */
    internal fun extractCellValue(cell: Cell?): Any? {
        if (cell == null) return null

        val effectiveCell = if (cell.cellType == CellType.FORMULA && formulaEvaluator != null) {
            try {
                formulaEvaluator!!.evaluateInCell(cell)
            } catch (_: Exception) {
                cell
            }
        } else {
            cell
        }

        return when (effectiveCell.cellType) {
            CellType.STRING -> {
                val value = effectiveCell.stringCellValue
                if (settings.blankAsNull && value.isBlank()) null else value
            }
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(effectiveCell)) {
                    effectiveCell.localDateTimeCellValue
                } else {
                    val numericValue = effectiveCell.numericCellValue
                    // Return as Long if the value is a whole number
                    if (numericValue == numericValue.toLong().toDouble()) {
                        numericValue.toLong()
                    } else {
                        numericValue
                    }
                }
            }
            CellType.BOOLEAN -> effectiveCell.booleanCellValue
            CellType.BLANK -> null
            CellType.ERROR -> null
            CellType.FORMULA -> effectiveCell.cellFormula  // fallback for unevaluated formulas
            else -> null
        }
    }
}
