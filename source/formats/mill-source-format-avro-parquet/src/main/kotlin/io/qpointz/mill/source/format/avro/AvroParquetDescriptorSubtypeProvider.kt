package io.qpointz.mill.source.format.avro

import com.fasterxml.jackson.databind.jsontype.NamedType
import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import io.qpointz.mill.source.format.parquet.ParquetFormatDescriptor

/**
 * SPI provider that registers Avro and Parquet format descriptor subtypes
 * with Jackson for polymorphic deserialization.
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`.
 */
class AvroParquetDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        NamedType(AvroFormatDescriptor::class.java, "avro"),
        NamedType(ParquetFormatDescriptor::class.java, "parquet"),
    )
}
