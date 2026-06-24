package io.qpointz.mill.data.odata.resolve

import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.metadata.service.MetadataContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EdmPropertyResolverCacheTest {

    @Test
    fun shouldCacheTableMetadataWhenCacheProvided() {
        val schemaFacetService = mock<SchemaFacetService>()
        val table = mock<SchemaTableWithFacets>()
        whenever(schemaFacetService.getTable("skymill", "cities", MetadataContext.global())).thenReturn(table)

        var loads = 0
        val memo = mutableMapOf<String, SchemaTableWithFacets?>()
        val cache = SchemaTableCache { key, loader ->
            memo.getOrPut(key) {
                loads++
                loader()
            }
        }

        val resolver = EdmPropertyResolver(schemaFacetService, cache)
        assertThat(resolver.resolveTable("skymill", "cities")).isSameAs(table)
        assertThat(resolver.resolveTable("skymill", "cities")).isSameAs(table)
        assertThat(resolver.columnIndex("skymill", "cities", "id")).isNull()

        verify(schemaFacetService, times(1)).getTable("skymill", "cities", MetadataContext.global())
        assertThat(loads).isEqualTo(1)
    }
}
