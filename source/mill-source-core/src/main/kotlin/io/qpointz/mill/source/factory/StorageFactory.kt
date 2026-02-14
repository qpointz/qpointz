package io.qpointz.mill.source.factory

import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.descriptor.StorageDescriptor

/**
 * SPI factory that creates a [BlobSource] from a [StorageDescriptor].
 *
 * Each storage backend module provides an implementation of this interface
 * registered via `META-INF/services/io.qpointz.mill.source.factory.StorageFactory`.
 *
 * The [SourceMaterializer] discovers all factories at runtime and selects
 * the one whose [descriptorType] matches the concrete descriptor class.
 *
 * @see io.qpointz.mill.source.descriptor.LocalStorageDescriptor
 * @see LocalStorageFactory
 */
interface StorageFactory {

    /**
     * The concrete [StorageDescriptor] subclass this factory supports.
     */
    val descriptorType: Class<out StorageDescriptor>

    /**
     * Creates a [BlobSource] from the given [descriptor].
     *
     * @param descriptor storage configuration
     * @return a ready-to-use [BlobSource]
     */
    fun create(descriptor: StorageDescriptor): BlobSource
}
