package io.qpointz.mill.source.format.text

import com.fasterxml.jackson.databind.jsontype.NamedType
import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider

/**
 * SPI provider that registers CSV, TSV, and FWF format descriptor subtypes
 * with Jackson for polymorphic deserialization.
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`.
 */
class TextDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        NamedType(CsvFormatDescriptor::class.java, "csv"),
        NamedType(TsvFormatDescriptor::class.java, "tsv"),
        NamedType(FwfFormatDescriptor::class.java, "fwf"),
    )
}
