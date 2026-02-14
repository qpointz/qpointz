package io.qpointz.mill.source.format.excel

/**
 * Column definition for an Excel sheet.
 *
 * @property name column name
 * @property index zero-based column index in the sheet
 */
data class SheetColumnDef(
    val name: String,
    val index: Int
)

/**
 * Configuration for reading a single Excel sheet.
 *
 * @property hasHeader      whether the first row contains column names (default `true`)
 * @property startRow       zero-based row index to start reading data (default `0`, or `1` if hasHeader)
 * @property columns        explicit column definitions; if empty, columns are auto-detected from header or generated
 * @property blankAsNull    treat blank cells as null (default `true`)
 * @property evaluateFormulas whether to evaluate formulas (default `true`)
 */
data class SheetSettings(
    val hasHeader: Boolean = true,
    val startRow: Int? = null,
    val columns: List<SheetColumnDef> = emptyList(),
    val blankAsNull: Boolean = true,
    val evaluateFormulas: Boolean = true
) {
    /**
     * Returns the effective data start row (after optional header).
     */
    val effectiveStartRow: Int
        get() = startRow ?: if (hasHeader) 1 else 0
}
