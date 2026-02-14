package io.qpointz.mill.source.format.parquet

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.FormatDescriptor

/**
 * Format descriptor for Apache Parquet files.
 *
 * YAML example:
 * ```yaml
 * format:
 *   type: parquet
 * ```
 *
 * Parquet files are self-describing — the schema is embedded in the file footer,
 * so no additional configuration is needed.
 */
@JsonTypeName("parquet")
class ParquetFormatDescriptor : FormatDescriptor
