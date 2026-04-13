package io.qpointz.mill.ai.data.sql

import io.qpointz.mill.data.schema.ModelRootWithFacets
import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.metadata.service.MetadataContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class EngineBackedSqlValidatorTest {

    private fun facetWithSkymillCities(): SchemaFacetService {
        val facet = mock<SchemaFacetService>()
        val schema = mock<SchemaWithFacets>()
        whenever(schema.schemaName).thenReturn("skymill")
        whenever(schema.tables).thenReturn(emptyList())
        val result = SchemaFacetResult(
            modelRoot = mock<ModelRootWithFacets>(),
            schemas = listOf(schema),
            unboundMetadata = emptyList(),
        )
        whenever(facet.getSchemas(any<MetadataContext>())).thenReturn(result)
        return facet
    }

    @Test
    fun `passes for trivial select without from`() {
        val facet = mock<SchemaFacetService>()
        whenever(facet.getSchemas(any<MetadataContext>())).thenReturn(
            SchemaFacetResult(mock(), emptyList(), emptyList()),
        )
        val v = EngineBackedSqlValidator(facet)
        assertThat(v.validate("SELECT 1").passed).isTrue()
    }

    @Test
    fun `fails for blank sql`() {
        val v = EngineBackedSqlValidator(mock())
        assertThat(v.validate("   ").passed).isFalse()
    }

    @Test
    fun `fails for invalid syntax`() {
        val v = EngineBackedSqlValidator(mock())
        assertThat(v.validate("SELECT FROM").passed).isFalse()
    }

    @Test
    fun `fails when table missing from catalog`() {
        val v = EngineBackedSqlValidator(facetWithSkymillCities())
        assertThat(v.validate("SELECT id FROM skymill.cities").passed).isFalse()
    }
}
