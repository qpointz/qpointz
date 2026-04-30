package io.qpointz.mill.data.schema.api

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.schema.DefaultMetadataEntityUrnCodec
import io.qpointz.mill.data.schema.ModelRootWithFacets
import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.repository.EntityReadSide
import io.qpointz.mill.metadata.repository.FacetReadSide
import io.qpointz.mill.proto.Table
import java.time.Instant
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import com.fasterxml.jackson.databind.ObjectMapper

@WebMvcTest(controllers = [SchemaExplorerController::class])
@ContextConfiguration(classes = [SchemaExplorerControllerWebMvcIT.TestApplication::class])
@Import(
    SchemaExplorerControllerWebMvcIT.SchemaFacetServiceTestConfig::class,
    SchemaExplorerService::class,
    SchemaExceptionHandler::class,
    JacksonAutoConfiguration::class
)
@AutoConfigureMockMvc(addFilters = false)
class SchemaExplorerControllerWebMvcIT {

    @SpringBootApplication
    class TestApplication

    /**
     * Registers a [SchemaFacetService] bean before [SchemaExplorerService] is evaluated so
     * `ConditionalOnBean(SchemaFacetService::class)` succeeds in slice tests (`MockitoBean` is
     * registered too late for that condition).
     */
    @TestConfiguration
    class SchemaFacetServiceTestConfig {
        @Bean
        fun schemaFacetService(): SchemaFacetService =
            Mockito.mock(SchemaFacetService::class.java)

        @Bean
        fun objectMapper(): ObjectMapper = ObjectMapper()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var schemaFacetService: SchemaFacetService

    private val urnCodec = DefaultMetadataEntityUrnCodec()
    private val fixedInstant = Instant.parse("2020-01-01T00:00:00Z")

    @MockitoBean
    private lateinit var schemaProvider: SchemaProvider

    @MockitoBean
    private lateinit var entityRead: EntityReadSide

    @MockitoBean
    private lateinit var facetReadSide: FacetReadSide

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
        whenever(entityRead.findAll()).thenReturn(listOf(entity))
        whenever(facetReadSide.findByEntity(any())).thenReturn(emptyList())

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
