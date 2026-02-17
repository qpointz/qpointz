package io.qpointz.mill.source.format.excel

import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.descriptor.FormatDescriptor
import io.qpointz.mill.source.factory.FormatHandlerFactory

/**
 * SPI factory that creates [ExcelFormatHandler] from [ExcelFormatDescriptor].
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.factory.FormatHandlerFactory`.
 */
class ExcelFormatHandlerFactory : FormatHandlerFactory {

    override val descriptorType: Class<out FormatDescriptor> = ExcelFormatDescriptor::class.java

    override fun create(descriptor: FormatDescriptor): FormatHandler {
        require(descriptor is ExcelFormatDescriptor) {
            "Expected ExcelFormatDescriptor, got ${descriptor::class.java.name}"
        }
        return ExcelFormatHandler(
            settings = descriptor.toSheetSettings(),
            selector = descriptor.toSheetSelector()
        )
    }
}
