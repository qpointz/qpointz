package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import io.qpointz.mill.source.factory.FormatHandlerFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.ServiceLoader

class SpiWiringTest {

    @Test
    fun shouldDiscoverDescriptorSubtypeProvider() {
        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
        val parquetProvider = providers.filterIsInstance<ParquetDescriptorSubtypeProvider>()
        assertEquals(1, parquetProvider.size)

        val subtypes = parquetProvider[0].subtypes()
        assertTrue(subtypes.any { it.type == ParquetFormatDescriptor::class.java })
    }

    @Test
    fun shouldDiscoverFormatHandlerFactory() {
        val factories = ServiceLoader.load(FormatHandlerFactory::class.java).toList()
        assertTrue(factories.any { it is ParquetFormatHandlerFactory })
    }

    @Test
    fun shouldCreateParquetFormatHandler_fromFactory() {
        val factory = ParquetFormatHandlerFactory()
        assertEquals(ParquetFormatDescriptor::class.java, factory.descriptorType)
        val handler = factory.create(ParquetFormatDescriptor())
        assertTrue(handler is ParquetFormatHandler)
    }

    @Test
    fun shouldThrow_whenWrongDescriptorType() {
        val factory = ParquetFormatHandlerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(object : io.qpointz.mill.source.descriptor.FormatDescriptor {})
        }
    }
}
