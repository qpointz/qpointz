package io.qpointz.mill.ai.data.schema

import io.qpointz.mill.data.schema.ModelRootWithFacets
import io.qpointz.mill.data.schema.SchemaColumnWithFacets
import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.Table
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SchemaFacetCatalogAdapterTest {

    @Test
    fun shouldReuseCachedSnapshot_acrossListCalls() {
        val svc = mock<SchemaFacetService>()
        whenever(svc.getSchemas()).thenReturn(fullSnapshot())
        val port = SchemaFacetCatalogAdapter(svc, ttlMillis = 60_000L)

        assertThat(port.listSchemas()).hasSize(1)
        assertThat(port.listTables("MONETA")).extracting<String> { it.tableName }
            .containsExactly("CLIENTS")
        assertThat(port.listColumns("moneta", "clients")).extracting<String> { it.columnName }
            .containsExactly("EMAIL")

        verify(svc, times(1)).getSchemas()
    }

    @Test
    fun shouldUseNarrowTablePath_whenCacheColdForListColumns() {
        val svc = mock<SchemaFacetService>()
        whenever(svc.getTable("moneta", "clients")).thenReturn(clientsTable())
        val port = SchemaFacetCatalogAdapter(svc, ttlMillis = 60_000L)

        val columns = port.listColumns("moneta", "clients")

        assertThat(columns).hasSize(1)
        assertThat(columns.single().columnName).isEqualTo("EMAIL")
        verify(svc, times(1)).getTable("moneta", "clients")
        verify(svc, times(0)).getSchemas()
    }

    @Test
    fun shouldUseNarrowSchemaPath_whenCacheColdForListTables() {
        val svc = mock<SchemaFacetService>()
        whenever(svc.getSchema("moneta")).thenReturn(monetaSchema())
        val port = SchemaFacetCatalogAdapter(svc, ttlMillis = 60_000L)

        val tables = port.listTables("moneta")

        assertThat(tables).hasSize(1)
        assertThat(tables.single().tableName).isEqualTo("CLIENTS")
        verify(svc, times(1)).getSchema("moneta")
        verify(svc, times(0)).getSchemas()
    }

    @Test
    fun shouldReloadAfterInvalidateCache() {
        val svc = mock<SchemaFacetService>()
        whenever(svc.getSchemas()).thenReturn(fullSnapshot())
        val port = SchemaFacetCatalogAdapter(svc, ttlMillis = 60_000L)

        port.listSchemas()
        port.invalidateCache()
        port.listSchemas()

        verify(svc, times(2)).getSchemas()
    }

    @Test
    fun shouldMatchSchemaNamesCaseInsensitively_fromCachedSnapshot() {
        val svc = mock<SchemaFacetService>()
        whenever(svc.getSchemas()).thenReturn(fullSnapshot())
        val port = SchemaFacetCatalogAdapter(svc, ttlMillis = 60_000L)

        port.listSchemas()
        assertThat(port.listTables("moneta")).hasSize(1)
        assertThat(port.listColumns("MONETA", "CLIENTS")).hasSize(1)
    }

    private fun fullSnapshot(): SchemaFacetResult =
        SchemaFacetResult(
            modelRoot = ModelRootWithFacets(
                metadataEntityId = "urn:mill/model/model:model-entity",
                metadata = null,
                facets = SchemaFacets.EMPTY,
            ),
            schemas = listOf(monetaSchema()),
            unboundMetadata = emptyList(),
        )

    private fun monetaSchema(): SchemaWithFacets =
        SchemaWithFacets(
            schemaName = "MONETA",
            tables = listOf(clientsTable()),
            metadata = null,
            facets = SchemaFacets.EMPTY,
        )

    private fun clientsTable(): SchemaTableWithFacets =
        SchemaTableWithFacets(
            schemaName = "MONETA",
            tableName = "CLIENTS",
            tableType = Table.TableTypeId.TABLE,
            columns = listOf(
                SchemaColumnWithFacets(
                    schemaName = "MONETA",
                    tableName = "CLIENTS",
                    columnName = "EMAIL",
                    fieldIndex = 0,
                    dataType = DataType.newBuilder()
                        .setNullability(DataType.Nullability.NULL)
                        .setType(
                            LogicalDataType.newBuilder()
                                .setTypeId(LogicalDataType.LogicalDataTypeId.STRING)
                                .build(),
                        )
                        .build(),
                    metadata = null,
                    facets = SchemaFacets.EMPTY,
                ),
            ),
            metadata = null,
            facets = SchemaFacets.EMPTY,
        )
}
