package io.qpointz.mill.ai.data.valuemap.refresh

import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.embedding.EmbeddingModelPersistenceDescriptor
import io.qpointz.mill.ai.valuemap.ColumnDistinctValueLoader
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.ai.valuemap.ValueMappingIndexingFacetTypes
import io.qpointz.mill.ai.valuemap.ValueMappingService
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingIndexedAttributeDiscovery
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingRefreshConfigurationBridge
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Schema
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class ValueMappingRefreshOrchestratorTest {

    private val now = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun `runOnDemand marks STALE when physical column missing`() {
        val urn = "urn:mill/model/attribute:skymill.cities.state"
        val facetRepo = mock<FacetRepository>()
        whenever(facetRepo.findByEntity(urn)).thenReturn(
            listOf(
                FacetAssignment(
                    uid = "00000000-0000-0000-0000-000000000001",
                    entityId = urn,
                    facetTypeKey = ValueMappingIndexingFacetTypes.AI_COLUMN_VALUE_MAPPING,
                    scopeKey = "urn:mill/metadata/scope:global",
                    mergeAction = MergeAction.SET,
                    payload = mapOf(
                        "context" to "ctx:",
                        "data" to mapOf(
                            "enabled" to true,
                            "refreshAtStartUp" to true,
                        ),
                        "nullValues" to mapOf("indexNull" to false),
                    ),
                    createdAt = now,
                    createdBy = null,
                    lastModifiedAt = now,
                    lastModifiedBy = null,
                ),
            ),
        )

        val stateRepo = mock<ValueMappingRefreshStateRepository>()
        val valueMappingService = mock<ValueMappingService>()
        val embeddingRepo = mock<ValueMappingEmbeddingRepository>()
        val harness = mock<EmbeddingHarness>()
        whenever(harness.persistence).thenReturn(
            EmbeddingModelPersistenceDescriptor("fp", "stub", "m", 2, null, null),
        )

        val schemaProvider = mock<SchemaProvider>()
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("skymill"))
        whenever(schemaProvider.getTable("skymill", "cities")).thenReturn(
            Table.newBuilder()
                .setSchemaName("skymill")
                .setName("cities")
                .addFields(Field.newBuilder().setName("wrong_column").build())
                .build(),
        )

        val orch = ValueMappingRefreshOrchestrator(
            refreshConfig = object : ValueMappingRefreshConfigurationBridge {
                override val refreshStartupEnabled: Boolean = true
                override val refreshScheduledDisabled: Boolean = false
            },
            attributeDiscovery = ValueMappingIndexedAttributeDiscovery { emptyList() },
            facetRepository = facetRepo,
            valueMappingService = valueMappingService,
            refreshStateRepository = stateRepo,
            embeddingRepository = embeddingRepo,
            embeddingHarness = harness,
            columnDistinctValueLoader = ColumnDistinctValueLoader { _, _, _, _ -> listOf("NY") },
            schemaProvider = schemaProvider,
        )

        orch.runOnDemand(urn)

        verify(stateRepo).markStale(eq(urn), any())
        verify(valueMappingService, never()).syncFromSource(any(), any(), any(), any())
    }

    @Test
    fun `runOnDemand resolves uppercase physical schema table column against lowercase catalog path`() {
        val urn = "urn:mill/model/attribute:skymill.cities.state"
        val facetRepo = mock<FacetRepository>()
        whenever(facetRepo.findByEntity(urn)).thenReturn(
            listOf(
                FacetAssignment(
                    uid = "00000000-0000-0000-0000-000000000001",
                    entityId = urn,
                    facetTypeKey = ValueMappingIndexingFacetTypes.AI_COLUMN_VALUE_MAPPING,
                    scopeKey = "urn:mill/metadata/scope:global",
                    mergeAction = MergeAction.SET,
                    payload = mapOf(
                        "context" to "ctx:",
                        "data" to mapOf(
                            "enabled" to true,
                            "refreshAtStartUp" to true,
                        ),
                        "nullValues" to mapOf("indexNull" to false),
                    ),
                    createdAt = now,
                    createdBy = null,
                    lastModifiedAt = now,
                    lastModifiedBy = null,
                ),
            ),
        )

        val stateRepo = mock<ValueMappingRefreshStateRepository>()
        val valueMappingService = mock<ValueMappingService>()
        val embeddingRepo = mock<ValueMappingEmbeddingRepository>()
        val harness = mock<EmbeddingHarness>()
        whenever(harness.persistence).thenReturn(
            EmbeddingModelPersistenceDescriptor("fp", "stub", "m", 2, null, null),
        )
        whenever(embeddingRepo.ensureEmbeddingModel(any(), any(), any(), any(), any(), any())).thenReturn(1L)

        val schemaProvider = mock<SchemaProvider>()
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("SKYMILL"))
        whenever(schemaProvider.getTable("SKYMILL", "cities")).thenReturn(null)
        whenever(schemaProvider.getSchema("SKYMILL")).thenReturn(
            Schema.newBuilder()
                .addTables(
                    Table.newBuilder()
                        .setSchemaName("SKYMILL")
                        .setName("CITIES")
                        .addFields(Field.newBuilder().setName("STATE").build())
                        .build(),
                )
                .build(),
        )

        val distinctLoadArgs = mutableListOf<Triple<String, String, String>>()
        val columnLoader = ColumnDistinctValueLoader { schema, table, column, _ ->
            distinctLoadArgs += Triple(schema, table, column)
            listOf("NY")
        }

        val orch = ValueMappingRefreshOrchestrator(
            refreshConfig = object : ValueMappingRefreshConfigurationBridge {
                override val refreshStartupEnabled: Boolean = true
                override val refreshScheduledDisabled: Boolean = false
            },
            attributeDiscovery = ValueMappingIndexedAttributeDiscovery { emptyList() },
            facetRepository = facetRepo,
            valueMappingService = valueMappingService,
            refreshStateRepository = stateRepo,
            embeddingRepository = embeddingRepo,
            embeddingHarness = harness,
            columnDistinctValueLoader = columnLoader,
            schemaProvider = schemaProvider,
        )

        orch.runOnDemand(urn)

        verify(stateRepo, never()).markStale(any(), any())
        verify(valueMappingService).syncFromSource(any(), any(), any(), any())
        assertEquals(
            listOf(Triple("SKYMILL", "CITIES", "STATE")),
            distinctLoadArgs,
        )
    }
}
