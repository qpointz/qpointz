package io.qpointz.mill.source.factory

import io.qpointz.mill.source.BlobToTableMapper
import io.qpointz.mill.source.descriptor.TableMappingDescriptor

/**
 * SPI factory that creates a [BlobToTableMapper] from a [TableMappingDescriptor].
 *
 * Registered via `META-INF/services/io.qpointz.mill.source.factory.TableMapperFactory`.
 *
 * @see io.qpointz.mill.source.descriptor.RegexTableMappingDescriptor
 * @see io.qpointz.mill.source.descriptor.DirectoryTableMappingDescriptor
 */
interface TableMapperFactory {

    /**
     * The concrete [TableMappingDescriptor] subclass this factory supports.
     */
    val descriptorType: Class<out TableMappingDescriptor>

    /**
     * Creates a [BlobToTableMapper] from the given [descriptor].
     *
     * @param descriptor table mapping configuration
     * @return a ready-to-use [BlobToTableMapper]
     */
    fun create(descriptor: TableMappingDescriptor): BlobToTableMapper
}
