package io.qpointz.mill.source.format.excel

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.FormatDescriptor

/**
 * Format descriptor for Excel files (.xlsx, .xls).
 *
 * ## Sheet selection
 *
 * Sheets to read can be specified using one of several strategies (evaluated
 * in priority order):
 *
 * 1. `allSheets: true` — select every sheet in the workbook
 * 2. `sheetPattern` — select sheets whose name matches a regex
 * 3. `sheetName` — select a single sheet by exact name
 * 4. `sheetIndex` — select a single sheet by zero-based index
 * 5. *(default)* — select the first sheet only
 *
 * Additionally, `excludeSheets` and `excludeSheetPattern` can be combined
 * with any of the above to remove specific sheets from the selection.
 *
 * When multiple sheets are selected, their rows are concatenated into a
 * single table. All selected sheets must share the same column structure.
 *
 * YAML examples:
 * ```yaml
 * # Read all sheets (e.g. monthly tabs)
 * format:
 *   type: excel
 *   allSheets: true
 *   hasHeader: true
 *
 * # Read sheets matching a pattern
 * format:
 *   type: excel
 *   sheetPattern: "2024_.*"
 *   excludeSheets:
 *     - "2024_Summary"
 *
 * # Read a single sheet by name
 * format:
 *   type: excel
 *   sheetName: "Sheet1"
 * ```
 *
 * @property hasHeader            whether the first row contains column names (default `true`)
 * @property sheetName            specific sheet to read by exact name
 * @property sheetIndex           specific sheet to read by zero-based index
 * @property sheetPattern         regex pattern to match sheet names
 * @property allSheets            select all sheets in the workbook (default `false`)
 * @property excludeSheets        list of sheet names to exclude
 * @property excludeSheetPattern  regex pattern for sheets to exclude
 */
@JsonTypeName("excel")
data class ExcelFormatDescriptor(
    @JsonProperty("hasHeader") val hasHeader: Boolean = true,
    @JsonProperty("sheetName") val sheetName: String? = null,
    @JsonProperty("sheetIndex") val sheetIndex: Int? = null,
    @JsonProperty("sheetPattern") val sheetPattern: String? = null,
    @JsonProperty("allSheets") val allSheets: Boolean = false,
    @JsonProperty("excludeSheets") val excludeSheets: List<String>? = null,
    @JsonProperty("excludeSheetPattern") val excludeSheetPattern: String? = null
) : FormatDescriptor {

    /**
     * Converts to [SheetSettings].
     */
    fun toSheetSettings(): SheetSettings = SheetSettings(
        hasHeader = hasHeader
    )

    /**
     * Creates the [SheetSelector] from the descriptor.
     *
     * Include criteria priority: `allSheets` > `sheetPattern` > `sheetName` > `sheetIndex` > first sheet.
     * Exclude criteria are built from `excludeSheets` and `excludeSheetPattern`.
     */
    fun toSheetSelector(): SheetSelector {
        val includeCriteria: List<SheetCriteria> = when {
            allSheets -> listOf(SheetCriteria.AnySheet)
            sheetPattern != null -> listOf(SheetCriteria.ByPattern(sheetPattern))
            sheetName != null -> listOf(SheetCriteria.ByName(sheetName))
            sheetIndex != null -> listOf(SheetCriteria.ByIndex(sheetIndex))
            else -> listOf(SheetCriteria.ByIndex(0))
        }

        val excludeCriteria = buildList {
            excludeSheets?.forEach { add(SheetCriteria.ByName(it)) }
            excludeSheetPattern?.let { add(SheetCriteria.ByPattern(it)) }
        }

        return SheetSelector(include = includeCriteria, exclude = excludeCriteria)
    }
}
