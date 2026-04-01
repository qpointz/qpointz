package io.qpointz.mill.data.schema.api

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SchemaExplorerServiceTest {

    private val schemaFacetService = mock<SchemaFacetService>()
    private val schemaProvider = mock<SchemaProvider>()
    private val entityRead = mock<EntityReadSide>()
    private val facetReadSide = mock<FacetReadSide>()
    private val objectMapper = ObjectMapper()
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
    fun `listSchemas throws bad request for malformed scope`() {
        assertThrows(MillStatusRuntimeException::class.java) {
            service.listSchemas(",", null, null, "direct")
        }
    }
}
