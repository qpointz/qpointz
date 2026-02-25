package io.qpointz.mill.source.format.text

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.FormatDescriptor

/**
 * Format descriptor for tab-separated value (TSV) files.
 *
 * Backed by [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 * TSV uses escape sequences instead of quoting — this is distinct from CSV
 * with a tab delimiter.
 *
 * YAML example:
 * ```yaml
 * format:
 *   type: tsv
 *   hasHeader: true
 *   lineJoiningEnabled: false
 * ```
 */
@JsonTypeName("tsv")
data class TsvFormatDescriptor(
    // Format
    @JsonProperty("escapeChar") val escapeChar: String? = null,
    @JsonProperty("escapedTabChar") val escapedTabChar: String? = null,
    @JsonProperty("comment") val comment: String? = null,
    @JsonProperty("lineSeparator") val lineSeparator: String? = null,
    @JsonProperty("normalizedNewline") val normalizedNewline: String? = null,
    // Parser / Common
    @JsonProperty("hasHeader") val hasHeader: Boolean = true,
    @JsonProperty("lineJoiningEnabled") val lineJoiningEnabled: Boolean? = null,
    @JsonProperty("nullValue") val nullValue: String? = null,
    @JsonProperty("skipEmptyLines") val skipEmptyLines: Boolean? = null,
    @JsonProperty("ignoreLeadingWhitespaces") val ignoreLeadingWhitespaces: Boolean? = null,
    @JsonProperty("ignoreTrailingWhitespaces") val ignoreTrailingWhitespaces: Boolean? = null,
    @JsonProperty("maxCharsPerColumn") val maxCharsPerColumn: Int? = null,
    @JsonProperty("maxColumns") val maxColumns: Int? = null,
    @JsonProperty("headers") val headers: List<String>? = null,
    @JsonProperty("numberOfRecordsToRead") val numberOfRecordsToRead: Long? = null,
    @JsonProperty("numberOfRowsToSkip") val numberOfRowsToSkip: Long? = null,
    @JsonProperty("lineSeparatorDetectionEnabled") val lineSeparatorDetectionEnabled: Boolean? = null,
    @JsonProperty("commentCollectionEnabled") val commentCollectionEnabled: Boolean? = null
) : FormatDescriptor {

    /**
     * Converts this descriptor to [TsvSettings].
     */
    fun toSettings(): TsvSettings = TsvSettings(
        escapeChar = parseChar(escapeChar),
        escapedTabChar = parseChar(escapedTabChar),
        comment = parseChar(comment),
        lineSeparator = lineSeparator,
        normalizedNewline = parseChar(normalizedNewline),
        hasHeader = hasHeader,
        lineJoiningEnabled = lineJoiningEnabled,
        nullValue = nullValue,
        skipEmptyLines = skipEmptyLines,
        ignoreLeadingWhitespaces = ignoreLeadingWhitespaces,
        ignoreTrailingWhitespaces = ignoreTrailingWhitespaces,
        maxCharsPerColumn = maxCharsPerColumn,
        maxColumns = maxColumns,
        headers = headers,
        numberOfRecordsToRead = numberOfRecordsToRead,
        numberOfRowsToSkip = numberOfRowsToSkip,
        lineSeparatorDetectionEnabled = lineSeparatorDetectionEnabled,
        commentCollectionEnabled = commentCollectionEnabled
    )
}
