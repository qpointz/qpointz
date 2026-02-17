package io.qpointz.mill.source.format.avro

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
        val avroProvider = providers.filterIsInstance<AvroDescriptorSubtypeProvider>()
        assertEquals(1, avroProvider.size)

        val subtypes = avroProvider[0].subtypes()
        assertTrue(subtypes.any { it.type == AvroFormatDescriptor::class.java })
    }

    @Test
    fun shouldDiscoverFormatHandlerFactory() {
        val factories = ServiceLoader.load(FormatHandlerFactory::class.java).toList()
        assertTrue(factories.any { it is AvroFormatHandlerFactory })
    }

    @Test
    fun shouldCreateAvroFormatHandler_fromFactory() {
        val factory = AvroFormatHandlerFactory()
        assertEquals(AvroFormatDescriptor::class.java, factory.descriptorType)
        val handler = factory.create(AvroFormatDescriptor())
        assertTrue(handler is AvroFormatHandler)
    }

    @Test
    fun shouldThrow_whenWrongDescriptorType() {
        val factory = AvroFormatHandlerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(object : io.qpointz.mill.source.descriptor.FormatDescriptor {})
        }
    }
}
