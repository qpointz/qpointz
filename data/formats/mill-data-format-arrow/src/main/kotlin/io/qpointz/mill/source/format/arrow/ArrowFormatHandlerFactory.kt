package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.descriptor.FormatDescriptor
import io.qpointz.mill.source.factory.FormatHandlerFactory

class ArrowFormatHandlerFactory : FormatHandlerFactory {
    override val descriptorType: Class<out FormatDescriptor> = ArrowFormatDescriptor::class.java

    override fun create(descriptor: FormatDescriptor): FormatHandler {
        require(descriptor is ArrowFormatDescriptor) {
            "Expected ArrowFormatDescriptor, got ${descriptor::class.java.name}"
        }
        return ArrowFormatHandler()
    }
}
