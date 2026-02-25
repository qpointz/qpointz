package io.qpointz.mill.source.format.arrow

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.FormatDescriptor

/**
 * Format descriptor for Apache Arrow IPC payloads (stream/file).
 */
@JsonTypeName("arrow")
class ArrowFormatDescriptor : FormatDescriptor
