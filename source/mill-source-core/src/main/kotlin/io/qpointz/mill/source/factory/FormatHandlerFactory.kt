package io.qpointz.mill.source.factory

import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.descriptor.FormatDescriptor

/**
 * SPI factory that creates a [FormatHandler] from a [FormatDescriptor].
 *
 * Each format module (text, Excel, Avro/Parquet) provides an implementation
 * registered via `META-INF/services/io.qpointz.mill.source.factory.FormatHandlerFactory`.
 *
 * The [SourceMaterializer] discovers all factories and selects the one
 * whose [descriptorType] matches the concrete descriptor class.
 *
 * @see FormatDescriptor
 */
interface FormatHandlerFactory {

    /**
     * The concrete [FormatDescriptor] subclass this factory supports.
     */
    val descriptorType: Class<out FormatDescriptor>

    /**
     * Creates a [FormatHandler] from the given [descriptor].
     *
     * @param descriptor format configuration
     * @return a ready-to-use [FormatHandler]
     */
    fun create(descriptor: FormatDescriptor): FormatHandler
}
