package io.qpointz.mill.data.schema.api

import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [SchemaExplorerController::class])
@ContextConfiguration(classes = [SchemaExplorerControllerWebMvcIT.TestApplication::class])
@Import(SchemaExplorerService::class, SchemaExceptionHandler::class)
@AutoConfigureMockMvc(addFilters = false)
class SchemaExplorerControllerWebMvcIT {

    @SpringBootApplication
    class TestApplication

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var schemaFacetService: SchemaFacetService

    @MockitoBean
    private lateinit var schemaProvider: SchemaProvider

    @MockitoBean
    private lateinit var metadataRepository: MetadataRepository

    @Test
    fun `shouldReturnGlobalContext`() {
        mockMvc.get("/api/v1/schema/context")
            .andExpect {
                status { isOk() }
                jsonPath("$.selectedContext") { value("global") }
                jsonPath("$.availableScopes[0].slug") { value("global") }
            }
    }

    @Test
    fun `shouldListSchemas`() {
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("sales"))
        whenever(metadataRepository.findAll()).thenReturn(
            listOf(MetadataEntity().apply {
                id = "meta-sales"
                schemaName = "sales"
            })
        )

        mockMvc.get("/api/v1/schema/schemas") {
            param("context", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value("sales") }
            jsonPath("$[0].entityType") { value("SCHEMA") }
            jsonPath("$[0].metadataEntityId") { value("meta-sales") }
        }
    }

    @Test
    fun `shouldReturnTreePayload`() {
        whenever(schemaFacetService.getSchemas(any())).thenReturn(
            SchemaFacetResult(
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
                                facets = SchemaFacets.EMPTY
                            )
                        ),
                        metadata = MetadataEntity().apply { id = "meta-sales" },
                        facets = SchemaFacets.EMPTY
                    )
                ),
                unboundMetadata = emptyList()
            )
        )

        mockMvc.get("/api/v1/schema/tree") {
            param("context", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value("sales") }
            jsonPath("$[0].tables[0].id") { value("sales.customers") }
        }
    }

    @Test
    fun `shouldReturnBadRequest_whenContextIsMalformed`() {
        mockMvc.get("/api/v1/schema/schemas") {
            param("context", ",")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value("BAD_REQUEST") }
        }
    }
}
