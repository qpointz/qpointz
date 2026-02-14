package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Test-only stub [FormatDescriptor] used in serialization tests.
 *
 * Real format descriptors live in their respective format modules
 * (e.g. `mill-source-format-text`), but we need a concrete subtype
 * for end-to-end serialization tests in `mill-source-core`.
 */
@JsonTypeName("stub")
data class StubFormatDescriptor(
    val delimiter: String = ","
) : FormatDescriptor
