package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.descriptor.FormatDescriptor
import io.qpointz.mill.source.factory.FormatHandlerFactory

/**
 * SPI factory that creates [CsvFormatHandler] from [CsvFormatDescriptor].
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.factory.FormatHandlerFactory`.
 */
class CsvFormatHandlerFactory : FormatHandlerFactory {

    override val descriptorType: Class<out FormatDescriptor> = CsvFormatDescriptor::class.java

    override fun create(descriptor: FormatDescriptor): FormatHandler {
        require(descriptor is CsvFormatDescriptor) {
            "Expected CsvFormatDescriptor, got ${descriptor::class.java.name}"
        }
        return CsvFormatHandler(descriptor.toSettings())
    }
}
