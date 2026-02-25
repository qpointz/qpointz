package io.qpointz.mill.source.format.avro

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.FormatDescriptor

/**
 * Format descriptor for Apache Avro files.
 *
 * YAML example:
 * ```yaml
 * format:
 *   type: avro
 * ```
 *
 * Avro files are self-describing — the schema is embedded in the file header,
 * so no additional configuration is needed.
 */
@JsonTypeName("avro")
class AvroFormatDescriptor : FormatDescriptor
