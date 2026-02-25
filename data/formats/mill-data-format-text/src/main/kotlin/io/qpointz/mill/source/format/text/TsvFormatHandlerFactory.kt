package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.descriptor.FormatDescriptor
import io.qpointz.mill.source.factory.FormatHandlerFactory

/**
 * SPI factory that creates [TsvFormatHandler] instances from [TsvFormatDescriptor].
 */
class TsvFormatHandlerFactory : FormatHandlerFactory {

    override val descriptorType: Class<out FormatDescriptor>
        get() = TsvFormatDescriptor::class.java

    override fun create(descriptor: FormatDescriptor): FormatHandler {
        val tsvDescriptor = descriptor as TsvFormatDescriptor
        return TsvFormatHandler(tsvDescriptor.toSettings())
    }
}
