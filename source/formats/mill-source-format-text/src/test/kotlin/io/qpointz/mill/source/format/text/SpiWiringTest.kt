package io.qpointz.mill.source.format.text

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
        val textProvider = providers.filterIsInstance<TextDescriptorSubtypeProvider>()
        assertEquals(1, textProvider.size)

        val subtypes = textProvider[0].subtypes()
        assertTrue(subtypes.any { it.type == CsvFormatDescriptor::class.java })
        assertTrue(subtypes.any { it.type == TsvFormatDescriptor::class.java })
        assertTrue(subtypes.any { it.type == FwfFormatDescriptor::class.java })
    }

    @Test
    fun shouldDiscoverFormatHandlerFactories() {
        val factories = ServiceLoader.load(FormatHandlerFactory::class.java).toList()
        assertTrue(factories.any { it is CsvFormatHandlerFactory })
        assertTrue(factories.any { it is TsvFormatHandlerFactory })
        assertTrue(factories.any { it is FwfFormatHandlerFactory })
    }

    @Test
    fun shouldCreateCsvFormatHandler_fromFactory() {
        val factory = CsvFormatHandlerFactory()
        assertEquals(CsvFormatDescriptor::class.java, factory.descriptorType)
        val handler = factory.create(CsvFormatDescriptor())
        assertTrue(handler is CsvFormatHandler)
    }

    @Test
    fun shouldCreateCsvFormatHandler_withCustomDelimiter() {
        val factory = CsvFormatHandlerFactory()
        val handler = factory.create(CsvFormatDescriptor(delimiter = "\\t", hasHeader = false))
        assertTrue(handler is CsvFormatHandler)
    }

    @Test
    fun shouldThrow_whenWrongDescriptorType_csv() {
        val factory = CsvFormatHandlerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(FwfFormatDescriptor(columns = listOf(FwfColumnDescriptor("x", 0, 5))))
        }
    }

    @Test
    fun shouldCreateTsvFormatHandler_fromFactory() {
        val factory = TsvFormatHandlerFactory()
        assertEquals(TsvFormatDescriptor::class.java, factory.descriptorType)
        val handler = factory.create(TsvFormatDescriptor())
        assertTrue(handler is TsvFormatHandler)
    }

    @Test
    fun shouldThrow_whenWrongDescriptorType_tsv() {
        val factory = TsvFormatHandlerFactory()
        assertThrows<ClassCastException> {
            factory.create(CsvFormatDescriptor())
        }
    }

    @Test
    fun shouldCreateFwfFormatHandler_fromFactory() {
        val factory = FwfFormatHandlerFactory()
        assertEquals(FwfFormatDescriptor::class.java, factory.descriptorType)
        val descriptor = FwfFormatDescriptor(
            columns = listOf(FwfColumnDescriptor("id", 0, 5))
        )
        val handler = factory.create(descriptor)
        assertTrue(handler is FwfFormatHandler)
    }

    @Test
    fun shouldThrow_whenWrongDescriptorType_fwf() {
        val factory = FwfFormatHandlerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(CsvFormatDescriptor())
        }
    }
}
