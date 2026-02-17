package io.qpointz.mill.source.format.parquet

import com.fasterxml.jackson.databind.jsontype.NamedType
import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider

/**
 * SPI provider that registers the Parquet format descriptor subtype
 * with Jackson for polymorphic deserialization.
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`.
 */
class ParquetDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        NamedType(ParquetFormatDescriptor::class.java, "parquet"),
    )
}
