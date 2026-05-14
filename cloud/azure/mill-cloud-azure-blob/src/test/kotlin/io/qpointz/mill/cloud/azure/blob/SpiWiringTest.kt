package io.qpointz.mill.cloud.azure.blob

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import io.qpointz.mill.source.factory.StorageFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.ServiceLoader

class SpiWiringTest {

    @Test
    fun shouldDiscoverAdlsStorageFactoryViaSpi() {
        val factories = ServiceLoader.load(StorageFactory::class.java).toList()
        assertTrue(
            factories.any { it is AdlsStorageFactory },
            "ServiceLoader should discover AdlsStorageFactory"
        )
    }

    @Test
    fun shouldDiscoverAdlsDescriptorSubtypeProviderViaSpi() {
        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
        assertTrue(
            providers.any { it is AdlsDescriptorSubtypeProvider },
            "ServiceLoader should discover AdlsDescriptorSubtypeProvider"
        )
    }

    @Test
    fun shouldRegisterAdlsSubtype() {
        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
        val allSubtypes = providers.flatMap { it.subtypes() }
        assertTrue(
            allSubtypes.any { it.type == AdlsStorageDescriptor::class.java },
            "Should register AdlsStorageDescriptor subtype"
        )
    }

    @Test
    fun shouldMapDescriptorTypeCorrectly() {
        val factory = AdlsStorageFactory()
        assertEquals(AdlsStorageDescriptor::class.java, factory.descriptorType)
    }
}
