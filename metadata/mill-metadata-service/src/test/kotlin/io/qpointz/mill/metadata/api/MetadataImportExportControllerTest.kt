package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.service.ImportResult
import io.qpointz.mill.metadata.service.MetadataImportService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart

@WebMvcTest(controllers = [MetadataImportExportController::class])
@AutoConfigureMockMvc(addFilters = false)
class MetadataImportExportControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var importService: MetadataImportService

    private val monetaYaml = """
        ---
        kind: MetadataEntity
        entityUrn: urn:mill/model/schema:moneta
        entityKind: schema
        facets:
          - uid: facet-uid-1
            facetTypeUrn: urn:mill/metadata/facet-type:descriptive
            scopeUrn: urn:mill/metadata/scope:global
            mergeAction: SET
            payload:
              displayName: Moneta
    """.trimIndent()

    @Test
    fun shouldImportMetadata_whenValidYamlFileProvided() {
        whenever(importService.import(any(), eq(ImportMode.MERGE), any()))
            .thenReturn(ImportResult(entitiesImported = 1, facetTypesImported = 0, errors = emptyList()))

        val file = MockMultipartFile("file", "test.yaml", "text/yaml", monetaYaml.toByteArray())

        mockMvc.multipart("/api/v1/metadata/import") {
            file(file)
        }.andExpect {
            status { isOk() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
            jsonPath("$.entitiesImported") { value(1) }
            jsonPath("$.facetTypesImported") { value(0) }
            jsonPath("$.errors") { isArray() }
        }
    }

    @Test
    fun shouldImportInReplaceMode_whenModeParamIsReplace() {
        whenever(importService.import(any(), eq(ImportMode.REPLACE), any()))
            .thenReturn(ImportResult(entitiesImported = 5, facetTypesImported = 0, errors = emptyList()))

        val file = MockMultipartFile("file", "test.yaml", "text/yaml", monetaYaml.toByteArray())

        mockMvc.multipart("/api/v1/metadata/import") {
            file(file)
            param("mode", "REPLACE")
        }.andExpect {
            status { isOk() }
            jsonPath("$.entitiesImported") { value(5) }
        }
    }

    @Test
    fun shouldIncludeErrors_whenImportHasNonFatalErrors() {
        whenever(importService.import(any(), any(), any()))
            .thenReturn(ImportResult(
                entitiesImported = 2,
                facetTypesImported = 0,
                errors = listOf("Failed to import entity: invalid type")
            ))

        val file = MockMultipartFile("file", "test.yaml", "text/yaml", monetaYaml.toByteArray())

        mockMvc.multipart("/api/v1/metadata/import") {
            file(file)
        }.andExpect {
            status { isOk() }
            jsonPath("$.errors[0]") { value("Failed to import entity: invalid type") }
        }
    }

    @Test
    fun shouldExportMetadata_whenGetExportCalled() {
        val exportedYaml = "entities:\n  - id: moneta\n    type: SCHEMA\n"
        whenever(importService.export(any())).thenReturn(exportedYaml)

        mockMvc.get("/api/v1/metadata/export")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith("text/yaml") }
            }
    }

    @Test
    fun shouldExportWithScopeParam_whenScopeParamProvided() {
        val exportedYaml = "entities: []\n"
        whenever(importService.export(any())).thenReturn(exportedYaml)

        mockMvc.get("/api/v1/metadata/export") {
            param("scope", "global")
        }.andExpect {
            status { isOk() }
        }
    }
}
