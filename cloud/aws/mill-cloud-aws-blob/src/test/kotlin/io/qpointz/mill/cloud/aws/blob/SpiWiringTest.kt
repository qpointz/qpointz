package io.qpointz.mill.cloud.aws.blob

import io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider
import io.qpointz.mill.source.factory.StorageFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.ServiceLoader

class SpiWiringTest {

    @Test
    fun shouldDiscoverS3StorageFactory_viaServiceLoader() {
        val factories = ServiceLoader.load(StorageFactory::class.java).toList()
        assertThat(factories)
            .anyMatch { it is S3StorageFactory }
    }

    @Test
    fun shouldDiscoverS3DescriptorSubtypeProvider_viaServiceLoader() {
        val providers = ServiceLoader.load(DescriptorSubtypeProvider::class.java).toList()
        assertThat(providers)
            .anyMatch { it is S3DescriptorSubtypeProvider }
    }

    @Test
    fun shouldRegisterS3DescriptorType_inFactory() {
        val factory = S3StorageFactory()
        assertThat(factory.descriptorType).isEqualTo(S3StorageDescriptor::class.java)
    }

    @Test
    fun shouldProvideS3NamedType_inSubtypeProvider() {
        val provider = S3DescriptorSubtypeProvider()
        val subtypes = provider.subtypes()
        assertThat(subtypes).hasSize(1)
        assertThat(subtypes[0].name).isEqualTo("s3")
        assertThat(subtypes[0].type).isEqualTo(S3StorageDescriptor::class.java)
    }
}
