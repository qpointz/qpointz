package io.qpointz.mill.data.odata.service.edm

import io.qpointz.mill.data.odata.annotation.EdmAnnotationModel
import io.qpointz.mill.data.odata.edm.EntityDataModelFactory
import io.qpointz.mill.data.odata.edm.SchemaEdmPackage
import io.qpointz.mill.metadata.service.MetadataContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration

class ODataEdmRegistryCacheTest {

    @Test
    fun shouldRebuildEdmOnEveryCallWhenCacheDisabled() {
        val factory = mock<EntityDataModelFactory>()
        val edm = mock<com.sdl.odata.api.edm.model.EntityDataModel>()
        val schemaPackage = SchemaEdmPackage(edm, EdmAnnotationModel.Builder().build())
        whenever(factory.buildPackageForSchema("skymill", MetadataContext.global())).thenReturn(schemaPackage)

        val cache = ODataEdmRegistryCache(factory, ODataEdmCache(enabled = false, ttl = null))
        val registry = cache.registryFor("skymill")

        registry.entityDataModel
        registry.entityDataModel

        verify(factory, times(2)).buildPackageForSchema("skymill", MetadataContext.global())
    }

    @Test
    fun shouldCacheEdmPerSchemaWhenEnabled() {
        val factory = mock<EntityDataModelFactory>()
        val edm = mock<com.sdl.odata.api.edm.model.EntityDataModel>()
        val schemaPackage = SchemaEdmPackage(edm, EdmAnnotationModel.Builder().build())
        whenever(factory.buildPackageForSchema("skymill", MetadataContext.global())).thenReturn(schemaPackage)

        val cache = ODataEdmRegistryCache(
            factory,
            ODataEdmCache(enabled = true, ttl = Duration.ofMinutes(2)),
        )
        val registry = cache.registryFor("skymill")

        assertThat(registry.entityDataModel).isSameAs(edm)
        assertThat(registry.entityDataModel).isSameAs(edm)

        verify(factory, times(1)).buildPackageForSchema("skymill", MetadataContext.global())
    }
}
