package io.qpointz.mill.cloud.gcp.blob

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import tools.jackson.databind.jsontype.NamedType

/**
 * SPI provider that registers [GcsStorageDescriptor] with the
 * Jackson polymorphic type system under the `"gcs"` discriminator.
 */
class GcsDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        NamedType(GcsStorageDescriptor::class.java, "gcs")
    )
}
