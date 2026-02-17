package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.databind.jsontype.NamedType

/**
 * Test-only SPI provider that registers [StubFormatDescriptor].
 *
 * Demonstrates how third-party modules contribute format descriptors
 * through the SPI mechanism.
 */
class TestDescriptorSubtypeProvider : DescriptorSubtypeProvider {

    override fun subtypes(): List<NamedType> = listOf(
        NamedType(StubFormatDescriptor::class.java, "stub"),
    )
}
