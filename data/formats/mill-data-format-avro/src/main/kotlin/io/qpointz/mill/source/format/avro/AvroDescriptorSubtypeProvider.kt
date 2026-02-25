package io.qpointz.mill.source.format.avro

import com.fasterxml.jackson.databind.jsontype.NamedType
import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider

/**
 * SPI provider that registers the Avro format descriptor subtype
 * with Jackson for polymorphic deserialization.
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`.
 */
class AvroDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        NamedType(AvroFormatDescriptor::class.java, "avro"),
    )
}
