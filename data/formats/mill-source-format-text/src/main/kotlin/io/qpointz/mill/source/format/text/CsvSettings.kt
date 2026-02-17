package io.qpointz.mill.source.format.text

import com.univocity.parsers.csv.CsvFormat
import com.univocity.parsers.csv.CsvParserSettings
import com.univocity.parsers.csv.CsvWriterSettings
import com.univocity.parsers.csv.UnescapedQuoteHandling

/**
 * Configuration for CSV parsing and writing, backed by
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * All properties are nullable — when `null`, the Univocity default applies.
 * The [hasHeader] property defaults to `true` and controls whether the first
 * row is treated as a header (read as column names / written as header).
 *
 * ## Format-level properties (CsvFormat)
 * @property delimiter                    field delimiter (default `,`)
 * @property quote                        quote character (default `"`)
 * @property quoteEscape                  escape character inside quoted fields (default `"`)
 * @property charToEscapeQuoteEscaping    escape for the quote-escape character itself
 * @property comment                      comment line character
 * @property lineSeparator                line separator string
 * @property normalizedNewline            normalized newline character
 *
 * ## Parser-level properties (CsvParserSettings / CommonParserSettings)
 * @property hasHeader                    whether the first row contains column names (default `true`)
 * @property emptyValue                   replacement string for empty quoted values
 * @property nullValue                    replacement string for null values
 * @property skipEmptyLines               skip blank lines
 * @property ignoreLeadingWhitespaces     trim leading whitespace from values
 * @property ignoreTrailingWhitespaces    trim trailing whitespace from values
 * @property ignoreLeadingWhitespacesInQuotes   trim leading whitespace inside quotes
 * @property ignoreTrailingWhitespacesInQuotes  trim trailing whitespace inside quotes
 * @property escapeUnquotedValues         process escape sequences in unquoted values
 * @property keepEscapeSequences          retain raw escape sequences
 * @property keepQuotes                   keep enclosing quote characters in parsed values
 * @property normalizeLineEndingsWithinQuotes   normalize line endings inside quoted values
 * @property unescapedQuoteHandling       how to handle unescaped quotes (enum)
 * @property maxCharsPerColumn            safety limit for characters per column
 * @property maxColumns                   safety limit for number of columns
 * @property headers                      explicit column names (overrides header row)
 * @property numberOfRecordsToRead        stop after N records (-1 = unlimited)
 * @property numberOfRowsToSkip           skip N rows before parsing
 * @property lineSeparatorDetectionEnabled auto-detect line separator
 * @property delimiterDetectionEnabled    auto-detect delimiter character
 * @property quoteDetectionEnabled        auto-detect quote character
 * @property commentCollectionEnabled     collect comment lines
 */
data class CsvSettings(
    // Format
    val delimiter: Char? = null,
    val quote: Char? = null,
    val quoteEscape: Char? = null,
    val charToEscapeQuoteEscaping: Char? = null,
    val comment: Char? = null,
    val lineSeparator: String? = null,
    val normalizedNewline: Char? = null,
    // Parser / Common
    val hasHeader: Boolean = true,
    val emptyValue: String? = null,
    val nullValue: String? = null,
    val skipEmptyLines: Boolean? = null,
    val ignoreLeadingWhitespaces: Boolean? = null,
    val ignoreTrailingWhitespaces: Boolean? = null,
    val ignoreLeadingWhitespacesInQuotes: Boolean? = null,
    val ignoreTrailingWhitespacesInQuotes: Boolean? = null,
    val escapeUnquotedValues: Boolean? = null,
    val keepEscapeSequences: Boolean? = null,
    val keepQuotes: Boolean? = null,
    val normalizeLineEndingsWithinQuotes: Boolean? = null,
    val unescapedQuoteHandling: CsvUnescapedQuoteHandling? = null,
    val maxCharsPerColumn: Int? = null,
    val maxColumns: Int? = null,
    val headers: List<String>? = null,
    val numberOfRecordsToRead: Long? = null,
    val numberOfRowsToSkip: Long? = null,
    val lineSeparatorDetectionEnabled: Boolean? = null,
    val delimiterDetectionEnabled: Boolean? = null,
    val quoteDetectionEnabled: Boolean? = null,
    val commentCollectionEnabled: Boolean? = null
) {

    /**
     * Applies format-level settings to a [CsvFormat].
     */
    private fun applyFormat(fmt: CsvFormat) {
        delimiter?.let { fmt.setDelimiter(it) }
        quote?.let { fmt.quote = it }
        quoteEscape?.let { fmt.quoteEscape = it }
        charToEscapeQuoteEscaping?.let { fmt.charToEscapeQuoteEscaping = it }
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
     * Applies common parser-only settings.
     */
    private fun applyParserCommon(s: com.univocity.parsers.common.CommonParserSettings<*>) {
        applyCommon(s)
        numberOfRecordsToRead?.let { s.numberOfRecordsToRead = it }
        numberOfRowsToSkip?.let { s.numberOfRowsToSkip = it }
        lineSeparatorDetectionEnabled?.let { s.setLineSeparatorDetectionEnabled(it) }
        commentCollectionEnabled?.let { s.setCommentCollectionEnabled(it) }
    }

    /**
     * Creates a configured Univocity [CsvParserSettings].
     * Header extraction is always disabled — the mill schema manages headers externally.
     */
    fun toParserSettings(): CsvParserSettings {
        val s = CsvParserSettings()
        applyFormat(s.format)
        applyParserCommon(s)
        s.isHeaderExtractionEnabled = false

        emptyValue?.let { s.emptyValue = it }
        ignoreLeadingWhitespacesInQuotes?.let { s.setIgnoreLeadingWhitespacesInQuotes(it) }
        ignoreTrailingWhitespacesInQuotes?.let { s.setIgnoreTrailingWhitespacesInQuotes(it) }
        escapeUnquotedValues?.let { s.isEscapeUnquotedValues = it }
        keepEscapeSequences?.let { s.setKeepEscapeSequences(it) }
        keepQuotes?.let { s.keepQuotes = it }
        normalizeLineEndingsWithinQuotes?.let { s.isNormalizeLineEndingsWithinQuotes = it }
        unescapedQuoteHandling?.let { s.unescapedQuoteHandling = it.toUnivocity() }
        delimiterDetectionEnabled?.let { s.setDelimiterDetectionEnabled(it) }
        quoteDetectionEnabled?.let { s.setQuoteDetectionEnabled(it) }

        return s
    }

    /**
     * Creates a configured Univocity [CsvWriterSettings].
     */
    fun toWriterSettings(): CsvWriterSettings {
        val s = CsvWriterSettings()
        applyFormat(s.format)
        applyCommon(s)

        emptyValue?.let { s.emptyValue = it }
        escapeUnquotedValues?.let { s.isEscapeUnquotedValues = it }
        normalizeLineEndingsWithinQuotes?.let { s.isNormalizeLineEndingsWithinQuotes = it }

        return s
    }
}

/**
 * Mill-level enum for Univocity's [UnescapedQuoteHandling].
 *
 * Decouples the YAML configuration from the Univocity class.
 */
enum class CsvUnescapedQuoteHandling {
    STOP_AT_CLOSING_QUOTE,
    BACK_TO_DELIMITER,
    STOP_AT_DELIMITER,
    SKIP_VALUE,
    RAISE_ERROR;

    /** Converts to the corresponding Univocity enum value. */
    fun toUnivocity(): UnescapedQuoteHandling = when (this) {
        STOP_AT_CLOSING_QUOTE -> UnescapedQuoteHandling.STOP_AT_CLOSING_QUOTE
        BACK_TO_DELIMITER -> UnescapedQuoteHandling.BACK_TO_DELIMITER
        STOP_AT_DELIMITER -> UnescapedQuoteHandling.STOP_AT_DELIMITER
        SKIP_VALUE -> UnescapedQuoteHandling.SKIP_VALUE
        RAISE_ERROR -> UnescapedQuoteHandling.RAISE_ERROR
    }
}
