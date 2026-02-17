package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Describes a file format for reading blobs as records or vectors.
 *
 * Format-specific subtypes (e.g. `CsvFormatDescriptor`, `ParquetFormatDescriptor`)
 * live in their respective format modules and are discovered via SPI
 * (`ServiceLoader`). Each subtype must be annotated with
 * [com.fasterxml.jackson.annotation.JsonTypeName] to provide its discriminator value.
 *
 * The `type` property in YAML/JSON selects the concrete implementation:
 * ```yaml
 * format:
 *   type: csv
 *   delimiter: ","
 *   hasHeader: true
 * ```
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
interface FormatDescriptor
