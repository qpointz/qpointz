package io.qpointz.mill.metadata.service

import tools.jackson.databind.json.JsonMapper
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.repository.FacetReadSide
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class FacetTypeManagementServiceTest {

    private val facetCatalog: FacetCatalog = mock()
    private val definitionRepository: FacetTypeDefinitionRepository = mock()
    private val facetTypeRepository: FacetTypeRepository = mock()
    private val facetReadSide: FacetReadSide = mock()
    private val service = FacetTypeManagementService(
        facetCatalog,
        definitionRepository,
        facetTypeRepository,
        facetReadSide,
        JsonMapper.builder().findAndAddModules().build()
    )

    @Test
    fun shouldRejectCreateWhenDefinitionKeyAlreadyExists() {
        whenever(definitionRepository.findByKey(any())).thenAnswer { inv ->
            val k = inv.getArgument<String>(0)
            if (k.contains("governance", ignoreCase = true)) sampleDefinition(k) else null
        }
        whenever(facetTypeRepository.findByKey(any())).thenReturn(null)

        assertThrows(MillStatusRuntimeException::class.java) {
            service.create(manifest(typeKey = "governance"))
        }

        verify(facetCatalog, never()).registerDefinition(any())
    }

    @Test
    fun shouldCreate_whenShortTypeKey_resolvesUrnBeforeConflictCheck() {
        whenever(definitionRepository.findByKey(any())).thenReturn(null)
        whenever(facetTypeRepository.findByKey(any())).thenReturn(null)
        whenever(facetCatalog.registerDefinition(any())).thenAnswer { inv ->
            inv.getArgument<FacetTypeDefinition>(0)
        }

        val created = service.create(manifest(typeKey = "table"))

        assertEquals("${MetadataUrns.FACET_TYPE_PREFIX}table", created.typeKey)
        val fullKey = "${MetadataUrns.FACET_TYPE_PREFIX}table"
        verify(definitionRepository, atLeast(1)).findByKey(eq(fullKey))
        verify(facetTypeRepository, atLeast(1)).findByKey(eq(fullKey))
        verify(definitionRepository, never()).findByKey(eq("table"))
        verify(facetTypeRepository, never()).findByKey(eq("table"))
    }

    private fun sampleDefinition(typeKey: String) = FacetTypeDefinition(
        typeKey = typeKey,
        displayName = "x",
        description = "d",
        mandatory = false,
        enabled = true,
        targetCardinality = FacetTargetCardinality.SINGLE,
        applicableTo = null,
        contentSchema = null,
        schemaVersion = "1",
        createdAt = Instant.EPOCH,
        createdBy = "t",
        lastModifiedAt = Instant.EPOCH,
        lastModifiedBy = "t"
    )

    private fun manifest(typeKey: String): FacetTypeManifest = FacetTypeManifest(
        typeKey = typeKey,
        title = "Governance",
        description = "Governance metadata",
        enabled = true,
        mandatory = false,
        schemaVersion = "1.0",
        payload = FacetPayloadSchema(
            type = FacetSchemaType.OBJECT,
            title = "Governance payload",
            description = "Governance payload schema",
            fields = emptyList(),
            required = emptyList()
        )
    )
}
