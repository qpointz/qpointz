package io.qpointz.mill.source.format.excel

import org.apache.poi.ss.usermodel.Sheet

/**
 * Criteria for selecting sheets from a workbook.
 *
 * Implementations match against sheet properties (name, index)
 * to decide which sheets to include or exclude.
 */
sealed interface SheetCriteria {

    /**
     * Tests whether the given [sheet] matches this criteria.
     */
    fun matches(sheet: Sheet): Boolean

    /**
     * Matches any sheet.
     */
    data object AnySheet : SheetCriteria {
        override fun matches(sheet: Sheet): Boolean = true
    }

    /**
     * Matches a sheet by exact name.
     *
     * @property name the sheet name to match
     * @property ignoreCase whether to ignore case (default `true`)
     */
    data class ByName(
        val name: String,
        val ignoreCase: Boolean = true
    ) : SheetCriteria {
        override fun matches(sheet: Sheet): Boolean =
            sheet.sheetName.equals(name, ignoreCase)
    }

    /**
     * Matches a sheet by zero-based index.
     *
     * @property index the sheet index to match
     */
    data class ByIndex(val index: Int) : SheetCriteria {
        override fun matches(sheet: Sheet): Boolean {
            val workbook = sheet.workbook
            return workbook.getSheetIndex(sheet) == index
        }
    }

    /**
     * Matches sheets whose name matches a regex [pattern].
     *
     * @property pattern the regex to test against the sheet name
     */
    data class ByPattern(val pattern: Regex) : SheetCriteria {
        constructor(pattern: String) : this(Regex(pattern))

        override fun matches(sheet: Sheet): Boolean =
            pattern.matches(sheet.sheetName)
    }
}
