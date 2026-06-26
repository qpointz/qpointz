package io.qpointz.mill.data.schema.api

import tools.jackson.databind.json.JsonMapper
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.schema.DefaultMetadataEntityUrnCodec
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.repository.EntityReadSide
import io.qpointz.mill.metadata.repository.FacetReadSide
import java.time.Instant
import io.qpointz.mill.data.schema.ModelRootWithFacets
import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.data.schema.TreeFacetScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SchemaExplorerServiceTest {

    private val schemaFacetService = mock<SchemaFacetService>()
    private val schemaProvider = mock<SchemaProvider>()
    private val entityRead = mock<EntityReadSide>()
    private val facetReadSide = mock<FacetReadSide>()
    private val objectMapper = JsonMapper.builder().findAndAddModules().build()
    private val urnCodec = DefaultMetadataEntityUrnCodec()
    private val service = SchemaExplorerService(
        schemaFacetService,
        schemaProvider,
        entityRead,
        facetReadSide,
        objectMapper,
        urnCodec
    )

    private val fixedInstant = Instant.parse("2020-01-01T00:00:00Z")

    @Test
    fun `listSchemas returns schema entries with metadata ids`() {
        val schemaUrn = urnCodec.forSchema("sales")
        val entity = MetadataEntity(
            id = schemaUrn,
            kind = null,
            uuid = null,
            createdAt = fixedInstant,
            createdBy = null,
            lastModifiedAt = fixedInstant,
            lastModifiedBy = null
        )
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("sales"))
        whenever(entityRead.findAll()).thenReturn(listOf(entity))
        whenever(facetReadSide.findByEntity(any())).thenReturn(emptyList())

        val result = service.listSchemas("global", null, null, "direct")
        assertEquals(2, result.size)
        assertEquals(SchemaModelRoot.ENTITY_LOCAL_ID, result[0].id)
        assertEquals(MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID), result[0].metadataEntityId)
        assertEquals("sales", result[1].id)
        assertEquals(schemaUrn, result[1].metadataEntityId)
    }

    @Test
    fun `getTree omits facetsResolved when facetMode is none`() {
        val resolvedRow = FacetInstance(
            assignmentUuid = "assign-desc",
            entityId = urnCodec.forSchema("sales"),
            facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            scopeKey = MetadataUrns.SCOPE_GLOBAL,
            mergeAction = MergeAction.SET,
            payload = mapOf("displayName" to "Sales schema"),
            createdAt = fixedInstant,
            createdBy = null,
            lastModifiedAt = fixedInstant,
            lastModifiedBy = null,
            origin = FacetOrigin.CAPTURED,
            originId = "repo",
            assignmentUid = "assign-desc"
        )
        val facetsWithResolved = SchemaFacets(emptySet(), listOf(resolvedRow))
        whenever(schemaFacetService.getSchemaTree(any(), eq(TreeFacetScope.NONE))).thenReturn(
            SchemaFacetResult(
                modelRoot = ModelRootWithFacets(
                    metadataEntityId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID),
                    metadata = null,
                    facets = facetsWithResolved
                ),
                schemas = listOf(
                    SchemaWithFacets(
                        schemaName = "sales",
                        tables = listOf(
                            SchemaTableWithFacets(
                                schemaName = "sales",
                                tableName = "customers",
                                tableType = Table.TableTypeId.TABLE,
                                columns = emptyList(),
                                metadata = null,
                                facets = facetsWithResolved
                            )
                        ),
                        metadata = null,
                        facets = facetsWithResolved
                    )
                ),
                unboundMetadata = emptyList()
            )
        )

        val result = service.getTree("global", null, null, "none")

        verify(schemaFacetService).getSchemaTree(any(), eq(TreeFacetScope.NONE))

        assertNull(result.modelRoot.facets)
        assertNull(result.modelRoot.facetsResolved)
        assertNull(result.schemas.single().facets)
        assertNull(result.schemas.single().facetsResolved)
        assertNull(result.schemas.single().tables.single().facets)
        assertNull(result.schemas.single().tables.single().facetsResolved)
    }

    @Test
    fun `listSchemas throws bad request for malformed scope`() {
        assertThrows(MillStatusRuntimeException::class.java) {
            service.listSchemas(",", null, null, "direct")
        }
    }
}
