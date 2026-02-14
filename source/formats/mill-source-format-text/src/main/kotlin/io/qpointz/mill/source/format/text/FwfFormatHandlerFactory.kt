package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.descriptor.FormatDescriptor
import io.qpointz.mill.source.factory.FormatHandlerFactory

/**
 * SPI factory that creates [FwfFormatHandler] from [FwfFormatDescriptor].
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.factory.FormatHandlerFactory`.
 */
class FwfFormatHandlerFactory : FormatHandlerFactory {

    override val descriptorType: Class<out FormatDescriptor> = FwfFormatDescriptor::class.java

    override fun create(descriptor: FormatDescriptor): FormatHandler {
        require(descriptor is FwfFormatDescriptor) {
            "Expected FwfFormatDescriptor, got ${descriptor::class.java.name}"
        }
        return FwfFormatHandler(descriptor.toSettings())
    }
}
