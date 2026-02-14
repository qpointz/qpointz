package io.qpointz.mill.source.format.avro

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import io.qpointz.mill.source.factory.FormatHandlerFactory
import io.qpointz.mill.source.format.parquet.ParquetFormatDescriptor
import io.qpointz.mill.source.format.parquet.ParquetFormatHandler
import io.qpointz.mill.source.format.parquet.ParquetFormatHandlerFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.ServiceLoader

class SpiWiringTest {

    @Test
    fun shouldDiscoverDescriptorSubtypeProvider() {
        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
        val avroParquetProvider = providers.filterIsInstance<AvroParquetDescriptorSubtypeProvider>()
        assertEquals(1, avroParquetProvider.size)

        val subtypes = avroParquetProvider[0].subtypes()
        assertTrue(subtypes.any { it.type == AvroFormatDescriptor::class.java })
        assertTrue(subtypes.any { it.type == ParquetFormatDescriptor::class.java })
    }

    @Test
    fun shouldDiscoverFormatHandlerFactories() {
        val factories = ServiceLoader.load(FormatHandlerFactory::class.java).toList()
        assertTrue(factories.any { it is AvroFormatHandlerFactory })
        assertTrue(factories.any { it is ParquetFormatHandlerFactory })
    }

    @Test
    fun shouldCreateAvroFormatHandler_fromFactory() {
        val factory = AvroFormatHandlerFactory()
        assertEquals(AvroFormatDescriptor::class.java, factory.descriptorType)
        val handler = factory.create(AvroFormatDescriptor())
        assertTrue(handler is AvroFormatHandler)
    }

    @Test
    fun shouldThrow_whenWrongDescriptorType_avro() {
        val factory = AvroFormatHandlerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(ParquetFormatDescriptor())
        }
    }

    @Test
    fun shouldCreateParquetFormatHandler_fromFactory() {
        val factory = ParquetFormatHandlerFactory()
        assertEquals(ParquetFormatDescriptor::class.java, factory.descriptorType)
        val handler = factory.create(ParquetFormatDescriptor())
        assertTrue(handler is ParquetFormatHandler)
    }

    @Test
    fun shouldThrow_whenWrongDescriptorType_parquet() {
        val factory = ParquetFormatHandlerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(AvroFormatDescriptor())
        }
    }
}
