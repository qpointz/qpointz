package io.qpointz.mill.source.format.avro

import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.descriptor.FormatDescriptor
import io.qpointz.mill.source.factory.FormatHandlerFactory

/**
 * SPI factory that creates [AvroFormatHandler] from [AvroFormatDescriptor].
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.factory.FormatHandlerFactory`.
 */
class AvroFormatHandlerFactory : FormatHandlerFactory {

    override val descriptorType: Class<out FormatDescriptor> = AvroFormatDescriptor::class.java

    override fun create(descriptor: FormatDescriptor): FormatHandler {
        require(descriptor is AvroFormatDescriptor) {
            "Expected AvroFormatDescriptor, got ${descriptor::class.java.name}"
        }
        return AvroFormatHandler()
    }
}
