package io.qpointz.mill.source.format.excel

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
        val excelProvider = providers.filterIsInstance<ExcelDescriptorSubtypeProvider>()
        assertEquals(1, excelProvider.size)

        val subtypes = excelProvider[0].subtypes()
        assertTrue(subtypes.any { it.type == ExcelFormatDescriptor::class.java })
    }

    @Test
    fun shouldDiscoverFormatHandlerFactory() {
        val factories = ServiceLoader.load(FormatHandlerFactory::class.java).toList()
        assertTrue(factories.any { it is ExcelFormatHandlerFactory })
    }

    @Test
    fun shouldCreateExcelFormatHandler_fromFactory() {
        val factory = ExcelFormatHandlerFactory()
        assertEquals(ExcelFormatDescriptor::class.java, factory.descriptorType)
        val handler = factory.create(ExcelFormatDescriptor())
        assertTrue(handler is ExcelFormatHandler)
    }

    @Test
    fun shouldCreateHandler_withSheetName() {
        val factory = ExcelFormatHandlerFactory()
        val handler = factory.create(ExcelFormatDescriptor(sheetName = "Data"))
        assertTrue(handler is ExcelFormatHandler)
    }

    @Test
    fun shouldThrow_whenWrongDescriptorType() {
        val factory = ExcelFormatHandlerFactory()
        val wrongDescriptor = object : io.qpointz.mill.source.descriptor.FormatDescriptor {}
        assertThrows<IllegalArgumentException> {
            factory.create(wrongDescriptor)
        }
    }
}
