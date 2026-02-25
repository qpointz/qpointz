package io.qpointz.mill.source.format.arrow

import com.fasterxml.jackson.databind.jsontype.NamedType
import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider

class ArrowDescriptorSubtypeProvider : DescriptorSubtypeProvider {
    override fun subtypes(): List<NamedType> = listOf(
        NamedType(ArrowFormatDescriptor::class.java, "arrow")
    )
}
