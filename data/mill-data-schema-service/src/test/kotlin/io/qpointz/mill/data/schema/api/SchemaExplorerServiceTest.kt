package io.qpointz.mill.data.schema.api

import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.repository.MetadataRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.fasterxml.jackson.databind.ObjectMapper

class SchemaExplorerServiceTest {

    private val schemaFacetService = mock<SchemaFacetService>()
    private val schemaProvider = mock<SchemaProvider>()
    private val metadataRepository = mock<MetadataRepository>()
    private val objectMapper = ObjectMapper()
    private val service = SchemaExplorerService(schemaFacetService, schemaProvider, metadataRepository, objectMapper)

    @Test
    fun `listSchemas returns schema entries with metadata ids`() {
        val entity = MetadataEntity().apply { id = "meta-sales" }
        entity.schemaName = "sales"
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("sales"))
        whenever(metadataRepository.findAll()).thenReturn(listOf(entity))

        val result = service.listSchemas("global", "direct")
        assertEquals(1, result.size)
        assertEquals("sales", result[0].id)
        assertEquals("meta-sales", result[0].metadataEntityId)
    }

    @Test
    fun `listSchemas throws bad request for malformed context`() {
        assertThrows(MillStatusRuntimeException::class.java) {
            service.listSchemas(",", "direct")
        }
    }
}
