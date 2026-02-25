package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import io.qpointz.mill.source.factory.FormatHandlerFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.ServiceLoader

class SpiWiringTest {
    @Test
    fun shouldDiscoverDescriptorSubtypeProvider() {
        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
        val arrowProvider = providers.filterIsInstance<ArrowDescriptorSubtypeProvider>()
        assertEquals(1, arrowProvider.size)
        assertTrue(arrowProvider[0].subtypes().any { it.type == ArrowFormatDescriptor::class.java })
    }

    @Test
    fun shouldDiscoverFormatHandlerFactory() {
        val factories = ServiceLoader.load(FormatHandlerFactory::class.java).toList()
        assertTrue(factories.any { it is ArrowFormatHandlerFactory })
    }
}
