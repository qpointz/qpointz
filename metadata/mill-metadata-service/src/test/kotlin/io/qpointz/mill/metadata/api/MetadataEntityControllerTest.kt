package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.service.MetadataService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant
import java.util.Optional

@WebMvcTest(controllers = [MetadataEntityController::class])
@AutoConfigureMockMvc(addFilters = false)
class MetadataEntityControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var metadataService: MetadataService

    private val baseEntity = MetadataEntity(
        id = "myschema",
        type = MetadataType.SCHEMA,
        schemaName = "myschema",
        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2025-01-01T00:00:00Z"),
        facets = mutableMapOf(
            MetadataUrns.FACET_TYPE_DESCRIPTIVE to mutableMapOf(
                MetadataUrns.SCOPE_GLOBAL to mapOf("displayName" to "My Schema")
            )
        )
    )

    @Test
    fun shouldListEntities_whenGetEntities() {
        whenever(metadataService.findAll()).thenReturn(listOf(baseEntity))

        mockMvc.get("/api/v1/metadata/entities")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("myschema") }
                jsonPath("$[0].type") { value("SCHEMA") }
            }
    }

    @Test
    fun shouldFilterBySchema_whenSchemaParamProvided() {
        whenever(metadataService.findAll()).thenReturn(listOf(baseEntity))

        mockMvc.get("/api/v1/metadata/entities") {
            param("schema", "myschema")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].schemaName") { value("myschema") }
        }
    }

    @Test
    fun shouldReturnEmptyList_whenSchemaParamDoesNotMatch() {
        whenever(metadataService.findAll()).thenReturn(listOf(baseEntity))

        mockMvc.get("/api/v1/metadata/entities") {
            param("schema", "nonexistent")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun shouldGetEntityById_whenEntityExists() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))

        mockMvc.get("/api/v1/metadata/entities/myschema")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value("myschema") }
                jsonPath("$.schemaName") { value("myschema") }
            }
    }

    @Test
    fun shouldReturn404_whenEntityNotFound() {
        whenever(metadataService.findById(any())).thenReturn(Optional.empty())

        mockMvc.get("/api/v1/metadata/entities/unknown")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun shouldGetEntityFacets_whenEntityExists() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))

        mockMvc.get("/api/v1/metadata/entities/myschema/facets")
            .andExpect {
                status { isOk() }
                jsonPath("$['${MetadataUrns.FACET_TYPE_DESCRIPTIVE}'].facetType") {
                    value(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
                }
            }
    }

    @Test
    fun shouldReturn404ForFacets_whenEntityNotFound() {
        whenever(metadataService.findById(any())).thenReturn(Optional.empty())

        mockMvc.get("/api/v1/metadata/entities/unknown/facets")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun shouldGetFacetByType_whenEntityAndFacetExist() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))

        mockMvc.get("/api/v1/metadata/entities/myschema/facets/descriptive")
            .andExpect {
                status { isOk() }
                jsonPath("$.facetType") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
            }
    }

    @Test
    fun shouldReturn404ForFacetByType_whenFacetTypeAbsent() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))

        mockMvc.get("/api/v1/metadata/entities/myschema/facets/structural")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun shouldReturn404ForFacetByType_whenEntityNotFound() {
        whenever(metadataService.findById(any())).thenReturn(Optional.empty())

        mockMvc.get("/api/v1/metadata/entities/unknown/facets/descriptive")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun shouldDefaultToGlobalContext_whenContextParamOmitted() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))

        // No context param → defaults to global scope → facet under global scope is visible
        mockMvc.get("/api/v1/metadata/entities/myschema/facets/descriptive")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload") { isNotEmpty() }
            }
    }

    @Test
    fun shouldApplyContext_whenContextParamProvided() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))

        // context=global — same as default; facet payload should be present
        mockMvc.get("/api/v1/metadata/entities/myschema/facets/descriptive") {
            param("context", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$.payload") { isNotEmpty() }
        }
    }
}
