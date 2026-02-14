package io.qpointz.mill.source.format.text

import com.univocity.parsers.fixed.FixedWidthFields
import com.univocity.parsers.fixed.FixedWidthFormat
import com.univocity.parsers.fixed.FixedWidthParserSettings
import com.univocity.parsers.fixed.FixedWidthWriterSettings

/**
 * Defines a single column in a fixed-width file.
 *
 * @property name   column name
 * @property start  zero-based start position (inclusive)
 * @property end    zero-based end position (exclusive)
 */
data class FwfColumnDef(
    val name: String,
    val start: Int,
    val end: Int
) {
    init {
        require(start >= 0) { "start must be >= 0, got $start" }
        require(end > start) { "end ($end) must be > start ($start)" }
    }

    /** Width of this column in characters. */
    val width: Int get() = end - start
}

/**
 * Configuration for fixed-width file parsing and writing, backed by
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * Column positions are defined using start/end (converted to Univocity
 * [FixedWidthFields] internally via field lengths).
 *
 * All nullable properties use Univocity defaults when `null`.
 *
 * ## Column definitions
 * @property columns    ordered list of column definitions with positions
 *
 * ## Format-level properties (FixedWidthFormat)
 * @property padding               padding character (default space)
 * @property comment               comment line character
 * @property lineSeparator         line separator string
 * @property normalizedNewline     normalized newline character
 *
 * ## Parser-level properties (FixedWidthParserSettings / CommonParserSettings)
 * @property hasHeader                    whether the first line is a header (default `false`)
 * @property keepPadding                  retain padding character in parsed values
 * @property skipTrailingCharsUntilNewline discard trailing chars beyond record length
 * @property recordEndsOnNewline          newline terminates a record
 * @property useDefaultPaddingForHeaders  use default padding when parsing headers
 * @property nullValue                    replacement for null values
 * @property skipEmptyLines               skip blank lines
 * @property ignoreLeadingWhitespaces     trim leading whitespace
 * @property ignoreTrailingWhitespaces    trim trailing whitespace
 * @property maxCharsPerColumn            safety limit for characters per column
 * @property maxColumns                   safety limit for number of columns
 * @property numberOfRecordsToRead        stop after N records
 * @property numberOfRowsToSkip           skip N rows before parsing
 * @property lineSeparatorDetectionEnabled auto-detect line separator
 */
data class FwfSettings(
    val columns: List<FwfColumnDef>,
    // Format
    val padding: Char? = null,
    val comment: Char? = null,
    val lineSeparator: String? = null,
    val normalizedNewline: Char? = null,
    // Parser / Common
    val hasHeader: Boolean = false,
    val keepPadding: Boolean? = null,
    val skipTrailingCharsUntilNewline: Boolean? = null,
    val recordEndsOnNewline: Boolean? = null,
    val useDefaultPaddingForHeaders: Boolean? = null,
    val nullValue: String? = null,
    val skipEmptyLines: Boolean? = null,
    val ignoreLeadingWhitespaces: Boolean? = null,
    val ignoreTrailingWhitespaces: Boolean? = null,
    val maxCharsPerColumn: Int? = null,
    val maxColumns: Int? = null,
    val numberOfRecordsToRead: Long? = null,
    val numberOfRowsToSkip: Long? = null,
    val lineSeparatorDetectionEnabled: Boolean? = null
) {
    init {
        require(columns.isNotEmpty()) { "At least one column definition is required" }
    }

    /**
     * Builds Univocity [FixedWidthFields] from the column definitions.
     * Uses start/end positions which Univocity handles natively.
     */
    internal fun toFixedWidthFields(): FixedWidthFields {
        val names = columns.map { it.name }.toTypedArray()
        val lengths = columns.map { it.width }.toIntArray()
        return FixedWidthFields(names, lengths)
    }

    /**
     * Applies format-level settings.
     */
    private fun applyFormat(fmt: FixedWidthFormat) {
        padding?.let { fmt.padding = it }
        comment?.let { fmt.comment = it }
        lineSeparator?.let { fmt.setLineSeparator(it) }
        normalizedNewline?.let { fmt.normalizedNewline = it }
    }

    /**
     * Applies common parser/writer settings.
     */
    private fun applyCommon(s: com.univocity.parsers.common.CommonSettings<*>) {
        nullValue?.let { s.nullValue = it }
        skipEmptyLines?.let { s.setSkipEmptyLines(it) }
        ignoreLeadingWhitespaces?.let { s.setIgnoreLeadingWhitespaces(it) }
        ignoreTrailingWhitespaces?.let { s.setIgnoreTrailingWhitespaces(it) }
        maxCharsPerColumn?.let { s.maxCharsPerColumn = it }
        maxColumns?.let { s.maxColumns = it }
    }

    /**
     * Creates a configured Univocity [FixedWidthParserSettings].
     * Header extraction is always disabled — the mill schema manages headers externally.
     */
    fun toParserSettings(): FixedWidthParserSettings {
        val s = FixedWidthParserSettings(toFixedWidthFields())
        applyFormat(s.format)
        applyCommon(s)
        s.isHeaderExtractionEnabled = false

        keepPadding?.let { s.setKeepPadding(it) }
        skipTrailingCharsUntilNewline?.let { s.setSkipTrailingCharsUntilNewline(it) }
        recordEndsOnNewline?.let { s.setRecordEndsOnNewline(it) }
        useDefaultPaddingForHeaders?.let { s.setUseDefaultPaddingForHeaders(it) }
        numberOfRecordsToRead?.let { s.numberOfRecordsToRead = it }
        numberOfRowsToSkip?.let { s.numberOfRowsToSkip = it }
        lineSeparatorDetectionEnabled?.let { s.setLineSeparatorDetectionEnabled(it) }

        return s
    }

    /**
     * Creates a configured Univocity [FixedWidthWriterSettings].
     */
    fun toWriterSettings(): FixedWidthWriterSettings {
        val s = FixedWidthWriterSettings(toFixedWidthFields())
        applyFormat(s.format)
        applyCommon(s)
        return s
    }
}
