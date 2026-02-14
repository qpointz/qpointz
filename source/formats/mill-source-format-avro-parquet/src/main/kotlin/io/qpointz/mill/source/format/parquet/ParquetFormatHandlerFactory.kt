package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.descriptor.FormatDescriptor
import io.qpointz.mill.source.factory.FormatHandlerFactory

/**
 * SPI factory that creates [ParquetFormatHandler] from [ParquetFormatDescriptor].
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.factory.FormatHandlerFactory`.
 */
class ParquetFormatHandlerFactory : FormatHandlerFactory {

    override val descriptorType: Class<out FormatDescriptor> = ParquetFormatDescriptor::class.java

    override fun create(descriptor: FormatDescriptor): FormatHandler {
        require(descriptor is ParquetFormatDescriptor) {
            "Expected ParquetFormatDescriptor, got ${descriptor::class.java.name}"
        }
        return ParquetFormatHandler()
    }
}
