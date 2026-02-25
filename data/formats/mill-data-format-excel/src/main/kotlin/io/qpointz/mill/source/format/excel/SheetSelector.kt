package io.qpointz.mill.source.format.excel

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

/**
 * Selects sheets from a workbook based on include/exclude criteria.
 *
 * Sheets are selected if they match **any** include criteria and do **not**
 * match **any** exclude criteria. If no include criteria are specified,
 * all sheets are included by default.
 *
 * @property include criteria to include (empty = include all)
 * @property exclude criteria to exclude (empty = exclude none)
 */
data class SheetSelector(
    val include: List<SheetCriteria> = emptyList(),
    val exclude: List<SheetCriteria> = emptyList()
) {

    /**
     * Returns the list of sheets from the [workbook] that match the selection criteria.
     */
    fun select(workbook: Workbook): List<Sheet> {
        return workbook.sheets().filter { sheet ->
            val included = include.isEmpty() || include.any { it.matches(sheet) }
            val excluded = exclude.any { it.matches(sheet) }
            included && !excluded
        }
    }

    companion object {
        /** Selects all sheets. */
        val ALL = SheetSelector()

        /** Selects the first sheet only. */
        val FIRST = SheetSelector(include = listOf(SheetCriteria.ByIndex(0)))
    }
}

/**
 * Extension function to iterate over all sheets in a workbook.
 */
fun Workbook.sheets(): List<Sheet> = (0 until numberOfSheets).map { getSheetAt(it) }
