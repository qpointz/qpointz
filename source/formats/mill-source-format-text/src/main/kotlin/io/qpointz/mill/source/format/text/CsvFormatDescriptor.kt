package io.qpointz.mill.source.format.text

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.FormatDescriptor

/**
 * Format descriptor for delimited text (CSV) files.
 *
 * Backed by [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 * All properties are optional — when omitted, Univocity defaults apply.
 *
 * YAML example:
 * ```yaml
 * format:
 *   type: csv
 *   delimiter: ","
 *   hasHeader: true
 *   skipEmptyLines: true
 *   nullValue: ""
 * ```
 */
@JsonTypeName("csv")
data class CsvFormatDescriptor(
    // Format-level
    @JsonProperty("delimiter") val delimiter: String? = null,
    @JsonProperty("quote") val quote: String? = null,
    @JsonProperty("quoteEscape") val quoteEscape: String? = null,
    @JsonProperty("charToEscapeQuoteEscaping") val charToEscapeQuoteEscaping: String? = null,
    @JsonProperty("comment") val comment: String? = null,
    @JsonProperty("lineSeparator") val lineSeparator: String? = null,
    @JsonProperty("normalizedNewline") val normalizedNewline: String? = null,
    // Parser-level
    @JsonProperty("hasHeader") val hasHeader: Boolean = true,
    @JsonProperty("emptyValue") val emptyValue: String? = null,
    @JsonProperty("nullValue") val nullValue: String? = null,
    @JsonProperty("skipEmptyLines") val skipEmptyLines: Boolean? = null,
    @JsonProperty("ignoreLeadingWhitespaces") val ignoreLeadingWhitespaces: Boolean? = null,
    @JsonProperty("ignoreTrailingWhitespaces") val ignoreTrailingWhitespaces: Boolean? = null,
    @JsonProperty("ignoreLeadingWhitespacesInQuotes") val ignoreLeadingWhitespacesInQuotes: Boolean? = null,
    @JsonProperty("ignoreTrailingWhitespacesInQuotes") val ignoreTrailingWhitespacesInQuotes: Boolean? = null,
    @JsonProperty("escapeUnquotedValues") val escapeUnquotedValues: Boolean? = null,
    @JsonProperty("keepEscapeSequences") val keepEscapeSequences: Boolean? = null,
    @JsonProperty("keepQuotes") val keepQuotes: Boolean? = null,
    @JsonProperty("normalizeLineEndingsWithinQuotes") val normalizeLineEndingsWithinQuotes: Boolean? = null,
    @JsonProperty("unescapedQuoteHandling") val unescapedQuoteHandling: CsvUnescapedQuoteHandling? = null,
    @JsonProperty("maxCharsPerColumn") val maxCharsPerColumn: Int? = null,
    @JsonProperty("maxColumns") val maxColumns: Int? = null,
    @JsonProperty("headers") val headers: List<String>? = null,
    @JsonProperty("numberOfRecordsToRead") val numberOfRecordsToRead: Long? = null,
    @JsonProperty("numberOfRowsToSkip") val numberOfRowsToSkip: Long? = null,
    @JsonProperty("lineSeparatorDetectionEnabled") val lineSeparatorDetectionEnabled: Boolean? = null,
    @JsonProperty("delimiterDetectionEnabled") val delimiterDetectionEnabled: Boolean? = null,
    @JsonProperty("quoteDetectionEnabled") val quoteDetectionEnabled: Boolean? = null,
    @JsonProperty("commentCollectionEnabled") val commentCollectionEnabled: Boolean? = null
) : FormatDescriptor {

    /**
     * Converts this descriptor to [CsvSettings].
     * Strings representing single characters (e.g. delimiter, quote) are parsed as Char.
     * The special string `"\t"` is interpreted as tab.
     */
    fun toSettings(): CsvSettings = CsvSettings(
        delimiter = parseChar(delimiter),
        quote = parseChar(quote),
        quoteEscape = parseChar(quoteEscape),
        charToEscapeQuoteEscaping = parseChar(charToEscapeQuoteEscaping),
        comment = parseChar(comment),
        lineSeparator = lineSeparator,
        normalizedNewline = parseChar(normalizedNewline),
        hasHeader = hasHeader,
        emptyValue = emptyValue,
        nullValue = nullValue,
        skipEmptyLines = skipEmptyLines,
        ignoreLeadingWhitespaces = ignoreLeadingWhitespaces,
        ignoreTrailingWhitespaces = ignoreTrailingWhitespaces,
        ignoreLeadingWhitespacesInQuotes = ignoreLeadingWhitespacesInQuotes,
        ignoreTrailingWhitespacesInQuotes = ignoreTrailingWhitespacesInQuotes,
        escapeUnquotedValues = escapeUnquotedValues,
        keepEscapeSequences = keepEscapeSequences,
        keepQuotes = keepQuotes,
        normalizeLineEndingsWithinQuotes = normalizeLineEndingsWithinQuotes,
        unescapedQuoteHandling = unescapedQuoteHandling,
        maxCharsPerColumn = maxCharsPerColumn,
        maxColumns = maxColumns,
        headers = headers,
        numberOfRecordsToRead = numberOfRecordsToRead,
        numberOfRowsToSkip = numberOfRowsToSkip,
        lineSeparatorDetectionEnabled = lineSeparatorDetectionEnabled,
        delimiterDetectionEnabled = delimiterDetectionEnabled,
        quoteDetectionEnabled = quoteDetectionEnabled,
        commentCollectionEnabled = commentCollectionEnabled
    )
}

/**
 * Parses a YAML string as a single character, handling escape sequences.
 * Returns null if the input is null or empty.
 */
internal fun parseChar(value: String?): Char? {
    if (value.isNullOrEmpty()) return null
    return when (value) {
        "\\t" -> '\t'
        "\\n" -> '\n'
        "\\r" -> '\r'
        "\\\\" -> '\\'
        else -> value.first()
    }
}
