package io.qpointz.mill.metadata.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.excepions.statuses.MillStatusRuntimeException
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FacetTypeManagementServiceTest {

    private val facetCatalog: FacetCatalog = mock()
    private val facetTypeRepository: FacetTypeRepository = mock()
    private val service = FacetTypeManagementService(facetCatalog, facetTypeRepository, ObjectMapper())

    @Test
    fun shouldRejectCreateWhenSlugAliasAlreadyExists() {
        whenever(facetTypeRepository.existsByTypeKey("governance")).thenReturn(true)
        whenever(facetTypeRepository.existsByTypeKey("urn:mill/metadata/facet-type:governance")).thenReturn(false)

        assertThrows(MillStatusRuntimeException::class.java) {
            service.create(manifest(typeKey = "governance"))
        }

        verify(facetCatalog, never()).register(any())
    }

    @Test
    fun shouldRejectCreateWhenUrnAliasAlreadyExists() {
        whenever(facetTypeRepository.existsByTypeKey("governance")).thenReturn(false)
        whenever(facetTypeRepository.existsByTypeKey("urn:mill/metadata/facet-type:governance")).thenReturn(true)

        assertThrows(MillStatusRuntimeException::class.java) {
            service.create(manifest(typeKey = "governance"))
        }

        verify(facetCatalog, never()).register(any())
    }

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

