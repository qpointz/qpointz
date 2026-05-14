package io.qpointz.mill.cloud.aws.blob

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import tools.jackson.databind.jsontype.NamedType

/**
 * SPI provider that registers [S3StorageDescriptor] with Jackson
 * for polymorphic deserialization of [io.qpointz.mill.source.descriptor.StorageDescriptor].
 *
 * Discovered via `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`.
 */
class S3DescriptorSubtypeProvider : DescriptorSubtypeProvider {

    /**
     * Returns the `"s3"` named type for [S3StorageDescriptor].
     *
     * @return singleton list with the S3 storage descriptor named type
     */
    override fun subtypes(): List<NamedType> = listOf(
        NamedType(S3StorageDescriptor::class.java, "s3")
    )
}
