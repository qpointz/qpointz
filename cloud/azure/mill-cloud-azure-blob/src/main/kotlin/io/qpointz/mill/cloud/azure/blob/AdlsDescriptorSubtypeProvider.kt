package io.qpointz.mill.cloud.azure.blob

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import tools.jackson.databind.jsontype.NamedType

/**
 * SPI provider that registers the ADLS storage descriptor subtype
 * (`"adls"` → [AdlsStorageDescriptor]) with Jackson.
 */
class AdlsDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        NamedType(AdlsStorageDescriptor::class.java, "adls")
    )
}
