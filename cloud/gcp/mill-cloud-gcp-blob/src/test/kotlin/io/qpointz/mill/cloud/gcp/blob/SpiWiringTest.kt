package io.qpointz.mill.cloud.gcp.blob

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import io.qpointz.mill.source.factory.StorageFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.ServiceLoader

class SpiWiringTest {

    @Test
    fun shouldDiscoverGcsStorageFactoryViaSpi() {
        val factories = ServiceLoader.load(StorageFactory::class.java).toList()
        assertTrue(
            factories.any { it is GcsStorageFactory },
            "ServiceLoader should discover GcsStorageFactory"
        )
    }

    @Test
    fun shouldDiscoverGcsDescriptorSubtypeProviderViaSpi() {
        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
        assertTrue(
            providers.any { it is GcsDescriptorSubtypeProvider },
            "ServiceLoader should discover GcsDescriptorSubtypeProvider"
        )
    }

    @Test
    fun shouldRegisterGcsSubtypeWithCorrectName() {
        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
        val allSubtypes = providers.flatMap { it.subtypes() }
        val gcsType = allSubtypes.find { it.type == GcsStorageDescriptor::class.java }
        assertNotNull(gcsType, "Should register GcsStorageDescriptor as a named type")
        assertEquals("gcs", gcsType!!.name)
    }

    @Test
    fun shouldReportCorrectDescriptorType() {
        val factory = GcsStorageFactory()
        assertEquals(GcsStorageDescriptor::class.java, factory.descriptorType)
    }
}
