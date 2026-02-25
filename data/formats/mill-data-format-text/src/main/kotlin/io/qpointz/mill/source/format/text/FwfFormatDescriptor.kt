package io.qpointz.mill.source.format.text

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.FormatDescriptor

/**
 * Column definition for FWF format descriptor YAML.
 *
 * @property name  column name
 * @property start zero-based start position (inclusive)
 * @property end   zero-based end position (exclusive)
 */
data class FwfColumnDescriptor(
    @JsonProperty("name") val name: String,
    @JsonProperty("start") val start: Int,
    @JsonProperty("end") val end: Int
)

/**
 * Format descriptor for fixed-width files.
 *
 * Backed by [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 * All optional properties use Univocity defaults when omitted.
 *
 * YAML example:
 * ```yaml
 * format:
 *   type: fwf
 *   hasHeader: false
 *   padding: " "
 *   columns:
 *     - name: id
 *       start: 0
 *       end: 5
 *     - name: name
 *       start: 5
 *       end: 25
 * ```
 */
@JsonTypeName("fwf")
data class FwfFormatDescriptor(
    @JsonProperty("columns") val columns: List<FwfColumnDescriptor> = emptyList(),
    // Format
    @JsonProperty("padding") val padding: String? = null,
    @JsonProperty("comment") val comment: String? = null,
    @JsonProperty("lineSeparator") val lineSeparator: String? = null,
    @JsonProperty("normalizedNewline") val normalizedNewline: String? = null,
    // Parser / Common
    @JsonProperty("hasHeader") val hasHeader: Boolean = false,
    @JsonProperty("keepPadding") val keepPadding: Boolean? = null,
    @JsonProperty("skipTrailingCharsUntilNewline") val skipTrailingCharsUntilNewline: Boolean? = null,
    @JsonProperty("recordEndsOnNewline") val recordEndsOnNewline: Boolean? = null,
    @JsonProperty("useDefaultPaddingForHeaders") val useDefaultPaddingForHeaders: Boolean? = null,
    @JsonProperty("nullValue") val nullValue: String? = null,
    @JsonProperty("skipEmptyLines") val skipEmptyLines: Boolean? = null,
    @JsonProperty("ignoreLeadingWhitespaces") val ignoreLeadingWhitespaces: Boolean? = null,
    @JsonProperty("ignoreTrailingWhitespaces") val ignoreTrailingWhitespaces: Boolean? = null,
    @JsonProperty("maxCharsPerColumn") val maxCharsPerColumn: Int? = null,
    @JsonProperty("maxColumns") val maxColumns: Int? = null,
    @JsonProperty("numberOfRecordsToRead") val numberOfRecordsToRead: Long? = null,
    @JsonProperty("numberOfRowsToSkip") val numberOfRowsToSkip: Long? = null,
    @JsonProperty("lineSeparatorDetectionEnabled") val lineSeparatorDetectionEnabled: Boolean? = null
) : FormatDescriptor {

    /**
     * Converts this descriptor to [FwfSettings].
     */
    fun toSettings(): FwfSettings = FwfSettings(
        columns = columns.map { FwfColumnDef(it.name, it.start, it.end) },
        padding = parseChar(padding),
        comment = parseChar(comment),
        lineSeparator = lineSeparator,
        normalizedNewline = parseChar(normalizedNewline),
        hasHeader = hasHeader,
        keepPadding = keepPadding,
        skipTrailingCharsUntilNewline = skipTrailingCharsUntilNewline,
        recordEndsOnNewline = recordEndsOnNewline,
        useDefaultPaddingForHeaders = useDefaultPaddingForHeaders,
        nullValue = nullValue,
        skipEmptyLines = skipEmptyLines,
        ignoreLeadingWhitespaces = ignoreLeadingWhitespaces,
        ignoreTrailingWhitespaces = ignoreTrailingWhitespaces,
        maxCharsPerColumn = maxCharsPerColumn,
        maxColumns = maxColumns,
        numberOfRecordsToRead = numberOfRecordsToRead,
        numberOfRowsToSkip = numberOfRowsToSkip,
        lineSeparatorDetectionEnabled = lineSeparatorDetectionEnabled
    )
}
