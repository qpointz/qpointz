package io.qpointz.mill.data.schema.api

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.schema.DefaultMetadataEntityUrnCodec
import io.qpointz.mill.data.schema.ModelRootWithFacets
import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaModelRoot
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.proto.Table
import java.time.Instant
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [SchemaExplorerController::class])
@ContextConfiguration(classes = [SchemaExplorerControllerWebMvcIT.TestApplication::class])
@Import(SchemaExplorerService::class, SchemaExceptionHandler::class, JacksonAutoConfiguration::class)
@AutoConfigureMockMvc(addFilters = false)
class SchemaExplorerControllerWebMvcIT {

    @SpringBootApplication
    class TestApplication

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val urnCodec = DefaultMetadataEntityUrnCodec()
    private val fixedInstant = Instant.parse("2020-01-01T00:00:00Z")

    @MockitoBean
    private lateinit var schemaFacetService: SchemaFacetService

    @MockitoBean
    private lateinit var schemaProvider: SchemaProvider

    @MockitoBean
    private lateinit var metadataEntityRepository: MetadataEntityRepository

    @MockitoBean
    private lateinit var facetRepository: FacetRepository

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
        whenever(metadataEntityRepository.findAll()).thenReturn(listOf(entity))
        whenever(facetRepository.findByEntity(any())).thenReturn(emptyList())

        mockMvc.get("/api/v1/schema/schemas") {
            param("scope", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value(SchemaModelRoot.ENTITY_LOCAL_ID) }
            jsonPath("$[0].entityType") { value("MODEL") }
            jsonPath("$[0].metadataEntityId") { value(MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)) }
            jsonPath("$[1].id") { value("sales") }
            jsonPath("$[1].entityType") { value("SCHEMA") }
            jsonPath("$[1].metadataEntityId") { value(schemaUrn) }
        }
    }

    @Test
    fun `shouldReturnTreePayload`() {
        val schemaUrn = urnCodec.forSchema("sales")
        val schemaMetadata = MetadataEntity(
            id = schemaUrn,
            kind = null,
            uuid = null,
            createdAt = fixedInstant,
            createdBy = null,
            lastModifiedAt = fixedInstant,
            lastModifiedBy = null
        )
        whenever(schemaFacetService.getSchemas(any())).thenReturn(
            SchemaFacetResult(
                modelRoot = ModelRootWithFacets(
                    metadataEntityId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID),
                    metadata = null,
                    facets = SchemaFacets.EMPTY
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
                                facets = SchemaFacets.EMPTY
                            )
                        ),
                        metadata = schemaMetadata,
                        facets = SchemaFacets.EMPTY
                    )
                ),
                unboundMetadata = emptyList()
            )
        )

        mockMvc.get("/api/v1/schema/tree") {
            param("scope", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$.modelRoot.id") { value(SchemaModelRoot.ENTITY_LOCAL_ID) }
            jsonPath("$.modelRoot.entityType") { value("MODEL") }
            jsonPath("$.schemas[0].id") { value("sales") }
            jsonPath("$.schemas[0].tables[0].id") { value("sales.customers") }
        }
    }

    @Test
    fun `shouldReturnBadRequest_whenScopeIsMalformed`() {
        mockMvc.get("/api/v1/schema/schemas") {
            param("scope", ",")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value("BAD_REQUEST") }
        }
    }
}
