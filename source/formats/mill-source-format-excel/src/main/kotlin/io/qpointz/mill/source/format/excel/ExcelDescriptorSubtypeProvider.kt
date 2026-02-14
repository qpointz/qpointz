package io.qpointz.mill.source.format.excel

import com.fasterxml.jackson.databind.jsontype.NamedType
import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider

/**
 * SPI provider that registers Excel format descriptor subtypes
 * with Jackson for polymorphic deserialization.
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`.
 */
class ExcelDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        NamedType(ExcelFormatDescriptor::class.java, "excel"),
    )
}
