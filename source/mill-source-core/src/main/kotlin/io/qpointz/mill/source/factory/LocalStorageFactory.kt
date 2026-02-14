package io.qpointz.mill.source.factory

import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.LocalBlobSource
import io.qpointz.mill.source.descriptor.LocalStorageDescriptor
import io.qpointz.mill.source.descriptor.StorageDescriptor
import java.nio.file.Paths

/**
 * Built-in [StorageFactory] that creates a [LocalBlobSource] from
 * a [LocalStorageDescriptor].
 */
class LocalStorageFactory : StorageFactory {

    override val descriptorType: Class<out StorageDescriptor>
        get() = LocalStorageDescriptor::class.java

    override fun create(descriptor: StorageDescriptor): BlobSource {
        require(descriptor is LocalStorageDescriptor) {
            "Expected LocalStorageDescriptor, got ${descriptor::class.java.name}"
        }
        return LocalBlobSource(Paths.get(descriptor.rootPath))
    }
}
