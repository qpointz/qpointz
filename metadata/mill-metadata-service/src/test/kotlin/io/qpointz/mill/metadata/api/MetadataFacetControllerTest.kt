package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.service.FacetCatalog
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.Optional

@WebMvcTest(controllers = [MetadataFacetController::class])
@AutoConfigureMockMvc(addFilters = false)
class MetadataFacetControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var facetCatalog: FacetCatalog

    private val descriptiveDescriptor = FacetTypeDescriptor(
        typeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
        mandatory = true,
        enabled = true,
        displayName = "Descriptive",
        applicableTo = setOf(
            MetadataUrns.ENTITY_TYPE_SCHEMA,
            MetadataUrns.ENTITY_TYPE_TABLE,
            MetadataUrns.ENTITY_TYPE_ATTRIBUTE
        )
    )

    private val customDescriptor = FacetTypeDescriptor(
        typeKey = "urn:mill/metadata/facet-type:governance",
        mandatory = false,
        enabled = true,
        displayName = "Governance"
    )

    @Test
    fun shouldListFacetTypes_whenGetFacets() {
        whenever(facetCatalog.getAll()).thenReturn(listOf(descriptiveDescriptor, customDescriptor))

        mockMvc.get("/api/v1/metadata/facets")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].typeKey") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
            }
    }

    @Test
    fun shouldFilterByEnabledOnly_whenEnabledOnlyParamTrue() {
        whenever(facetCatalog.getEnabled()).thenReturn(listOf(descriptiveDescriptor))

        mockMvc.get("/api/v1/metadata/facets") {
            param("enabledOnly", "true")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].typeKey") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
        }
    }

    @Test
    fun shouldFilterByTargetType_whenTargetTypeParamProvided() {
        whenever(facetCatalog.getForTargetType(MetadataUrns.ENTITY_TYPE_TABLE))
            .thenReturn(listOf(descriptiveDescriptor))

        mockMvc.get("/api/v1/metadata/facets") {
            param("targetType", "table")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].typeKey") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
        }
    }

    @Test
    fun shouldGetFacetByShortKey_whenDescriptiveShortKeyProvided() {
        whenever(facetCatalog.get(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
            .thenReturn(Optional.of(descriptiveDescriptor))

        mockMvc.get("/api/v1/metadata/facets/descriptive")
            .andExpect {
                status { isOk() }
                jsonPath("$.typeKey") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
                jsonPath("$.mandatory") { value(true) }
            }
    }

    @Test
    fun shouldReturn404ForFacetByKey_whenNotFound() {
        whenever(facetCatalog.get(any())).thenReturn(Optional.empty())

        mockMvc.get("/api/v1/metadata/facets/nonexistent")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun shouldRegisterFacetType_whenValidBodyPosted() {
        val requestBody = """
            {
                "typeKey": "governance",
                "mandatory": false,
                "enabled": true,
                "displayName": "Governance"
            }
        """.trimIndent()

        mockMvc.post("/api/v1/metadata/facets") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isCreated() }
            jsonPath("$.typeKey") { value("urn:mill/metadata/facet-type:governance") }
        }
    }

    @Test
    fun shouldUpdateFacetType_whenValidBodyPut() {
        val requestBody = """
            {
                "typeKey": "governance",
                "mandatory": false,
                "enabled": false,
                "displayName": "Governance (disabled)"
            }
        """.trimIndent()

        mockMvc.put("/api/v1/metadata/facets/governance") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
            jsonPath("$.typeKey") { value("urn:mill/metadata/facet-type:governance") }
        }
    }

    @Test
    fun shouldDeleteFacetType_whenTypeIsNotMandatory() {
        mockMvc.delete("/api/v1/metadata/facets/governance")
            .andExpect {
                status { isNoContent() }
            }
    }

    @Test
    fun shouldReturn409_whenDeletingMandatoryFacetType() {
        doThrow(IllegalArgumentException("Facet type is mandatory and cannot be deleted"))
            .whenever(facetCatalog).delete(MetadataUrns.FACET_TYPE_DESCRIPTIVE)

        mockMvc.delete("/api/v1/metadata/facets/descriptive")
            .andExpect {
                status { isConflict() }
            }
    }
}
