package io.qpointz.mill.metadata.api

import io.qpointz.mill.data.schema.SchemaEntityTypeUrns
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
import io.qpointz.mill.metadata.service.FacetTypeManagementService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(controllers = [MetadataFacetController::class])
@AutoConfigureMockMvc(addFilters = false)
class MetadataFacetControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: FacetTypeManagementService

    private val descriptiveManifest = FacetTypeManifest(
        typeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
        enabled = true,
        mandatory = true,
        title = "Descriptive",
        description = "Human-readable metadata",
        applicableTo = setOf(
            SchemaEntityTypeUrns.SCHEMA,
            SchemaEntityTypeUrns.TABLE,
            SchemaEntityTypeUrns.ATTRIBUTE
        ).toList(),
        schemaVersion = "1.0",
        payload = FacetPayloadSchema(
            type = FacetSchemaType.OBJECT,
            title = "Descriptive payload",
            description = "Descriptive facet payload schema.",
            fields = emptyList(),
            required = emptyList()
        )
    )

    private val customManifest = FacetTypeManifest(
        typeKey = "urn:mill/metadata/facet-type:governance",
        enabled = true,
        mandatory = false,
        title = "Governance",
        description = "Governance metadata attached to entities.",
        applicableTo = null,
        schemaVersion = "1.0",
        payload = FacetPayloadSchema(
            type = FacetSchemaType.OBJECT,
            title = "Governance payload",
            description = "Governance facet payload schema.",
            fields = emptyList(),
            required = emptyList()
        )
    )

    @Test
    fun shouldListFacetTypes_whenGetFacets() {
        whenever(service.list(null, false)).thenReturn(listOf(descriptiveManifest, customManifest))

        mockMvc.get("/api/v1/metadata/facets")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].facetTypeUrn") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
            }
    }

    @Test
    fun shouldFilterByEnabledOnly_whenEnabledOnlyParamTrue() {
        whenever(service.list(null, true)).thenReturn(listOf(descriptiveManifest))

        mockMvc.get("/api/v1/metadata/facets") {
            param("enabledOnly", "true")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].facetTypeUrn") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
        }
    }

    @Test
    fun shouldFilterByTargetType_whenTargetTypeParamProvided() {
        whenever(service.list(SchemaEntityTypeUrns.TABLE, false))
            .thenReturn(listOf(descriptiveManifest))

        mockMvc.get("/api/v1/metadata/facets") {
            param("targetType", "table")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].facetTypeUrn") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
        }
    }

    @Test
    fun shouldGetFacetByShortKey_whenDescriptiveShortKeyProvided() {
        whenever(service.get(MetadataUrns.FACET_TYPE_DESCRIPTIVE)).thenReturn(descriptiveManifest)

        mockMvc.get("/api/v1/metadata/facets/descriptive")
            .andExpect {
                status { isOk() }
                jsonPath("$.facetTypeUrn") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
                jsonPath("$.mandatory") { value(true) }
            }
    }

    @Test
    fun shouldReturn404ForFacetByKey_whenNotFound() {
        whenever(service.get(any())).thenThrow(
            io.qpointz.mill.excepions.statuses.MillStatuses.notFoundRuntime("not found")
        )

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
                "title": "Governance",
                "description": "Governance metadata attached to entities.",
                "mandatory": false,
                "enabled": true,
                "schemaVersion": "1.0",
                "payload": {
                  "type": "OBJECT",
                  "title": "Governance payload",
                  "description": "Governance facet payload schema.",
                  "fields": [],
                  "required": []
                }
            }
        """.trimIndent()

        whenever(service.parseJson(any(), any())).thenReturn(customManifest.copy(typeKey = "governance"))
        whenever(service.create(any())).thenReturn(customManifest)

        mockMvc.post("/api/v1/metadata/facets") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isCreated() }
            jsonPath("$.facetTypeUrn") { value("urn:mill/metadata/facet-type:governance") }
        }
    }

    @Test
    fun shouldUpdateFacetType_whenValidBodyPut() {
        val requestBody = """
            {
                "typeKey": "governance",
                "title": "Governance (disabled)",
                "description": "Governance metadata attached to entities.",
                "mandatory": false,
                "enabled": false,
                "schemaVersion": "1.0",
                "payload": {
                  "type": "OBJECT",
                  "title": "Governance payload",
                  "description": "Governance facet payload schema.",
                  "fields": [],
                  "required": []
                }
            }
        """.trimIndent()

        whenever(service.parseJson(any(), any())).thenReturn(customManifest.copy(typeKey = "governance", enabled = false, title = "Governance (disabled)"))
        whenever(service.update(any(), any())).thenReturn(customManifest.copy(enabled = false, title = "Governance (disabled)"))

        mockMvc.put("/api/v1/metadata/facets/governance") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isOk() }
            jsonPath("$.facetTypeUrn") { value("urn:mill/metadata/facet-type:governance") }
        }
    }

    @Test
    fun shouldDeleteFacetType_whenTypeIsNotMandatory() {
        doNothing().whenever(service).delete(any())
        mockMvc.delete("/api/v1/metadata/facets/governance")
            .andExpect {
                status { isNoContent() }
            }
    }

    @Test
    fun shouldReturn409_whenDeletingMandatoryFacetType() {
        whenever(service.delete(MetadataUrns.FACET_TYPE_DESCRIPTIVE)).thenThrow(
            io.qpointz.mill.excepions.statuses.MillStatuses.conflictRuntime("mandatory")
        )

        mockMvc.delete("/api/v1/metadata/facets/descriptive")
            .andExpect {
                status { isConflict() }
            }
    }
}
