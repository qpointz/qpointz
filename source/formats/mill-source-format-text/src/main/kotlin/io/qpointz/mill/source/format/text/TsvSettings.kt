package io.qpointz.mill.source.format.text

import com.univocity.parsers.tsv.TsvFormat
import com.univocity.parsers.tsv.TsvParserSettings
import com.univocity.parsers.tsv.TsvWriterSettings

/**
 * Configuration for TSV parsing and writing, backed by
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * TSV uses escape sequences (`\t`, `\n`, `\r`, `\\`) instead of quoting.
 * All nullable properties use Univocity defaults when `null`.
 *
 * ## Format-level properties (TsvFormat)
 * @property escapeChar      character used for escaping `\t`, `\n`, `\r`, `\` (default `\`)
 * @property escapedTabChar  character following the escape that represents a tab (default `t`)
 * @property comment         comment line character
 * @property lineSeparator   line separator string
 * @property normalizedNewline normalized newline character
 *
 * ## Parser-level properties (TsvParserSettings / CommonParserSettings)
 * @property hasHeader                    whether the first row contains column names (default `true`)
 * @property lineJoiningEnabled           join lines ending with escape + newline
 * @property nullValue                    replacement for null values
 * @property skipEmptyLines               skip blank lines
 * @property ignoreLeadingWhitespaces     trim leading whitespace
 * @property ignoreTrailingWhitespaces    trim trailing whitespace
 * @property maxCharsPerColumn            safety limit for characters per column
 * @property maxColumns                   safety limit for number of columns
 * @property headers                      explicit column names
 * @property numberOfRecordsToRead        stop after N records
 * @property numberOfRowsToSkip           skip N rows before parsing
 * @property lineSeparatorDetectionEnabled auto-detect line separator
 * @property commentCollectionEnabled     collect comment lines
 */
data class TsvSettings(
    // Format
    val escapeChar: Char? = null,
    val escapedTabChar: Char? = null,
    val comment: Char? = null,
    val lineSeparator: String? = null,
    val normalizedNewline: Char? = null,
    // Parser / Common
    val hasHeader: Boolean = true,
    val lineJoiningEnabled: Boolean? = null,
    val nullValue: String? = null,
    val skipEmptyLines: Boolean? = null,
    val ignoreLeadingWhitespaces: Boolean? = null,
    val ignoreTrailingWhitespaces: Boolean? = null,
    val maxCharsPerColumn: Int? = null,
    val maxColumns: Int? = null,
    val headers: List<String>? = null,
    val numberOfRecordsToRead: Long? = null,
    val numberOfRowsToSkip: Long? = null,
    val lineSeparatorDetectionEnabled: Boolean? = null,
    val commentCollectionEnabled: Boolean? = null
) {

    /**
     * Applies format-level settings to a [TsvFormat].
     */
    private fun applyFormat(fmt: TsvFormat) {
        escapeChar?.let { fmt.escapeChar = it }
        escapedTabChar?.let { fmt.escapedTabChar = it }
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
        headers?.let { s.setHeaders(*it.toTypedArray()) }
    }

    /**
     * Creates a configured Univocity [TsvParserSettings].
     * Header extraction is always disabled — the mill schema manages headers externally.
     */
    fun toParserSettings(): TsvParserSettings {
        val s = TsvParserSettings()
        applyFormat(s.format)
        applyCommon(s)
        s.isHeaderExtractionEnabled = false

        lineJoiningEnabled?.let { s.isLineJoiningEnabled = it }
        numberOfRecordsToRead?.let { s.numberOfRecordsToRead = it }
        numberOfRowsToSkip?.let { s.numberOfRowsToSkip = it }
        lineSeparatorDetectionEnabled?.let { s.setLineSeparatorDetectionEnabled(it) }
        commentCollectionEnabled?.let { s.setCommentCollectionEnabled(it) }

        return s
    }

    /**
     * Creates a configured Univocity [TsvWriterSettings].
     */
    fun toWriterSettings(): TsvWriterSettings {
        val s = TsvWriterSettings()
        applyFormat(s.format)
        applyCommon(s)
        return s
    }
}
