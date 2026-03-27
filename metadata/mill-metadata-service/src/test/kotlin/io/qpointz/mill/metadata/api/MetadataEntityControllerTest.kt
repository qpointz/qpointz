package io.qpointz.mill.metadata.api

import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataOperationAuditRecord
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.MetadataFacetInstanceRow
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.metadata.service.MetadataEditService
import io.qpointz.mill.metadata.service.MetadataService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant
import java.util.Optional

@WebMvcTest(controllers = [MetadataEntityController::class, MetadataExceptionHandler::class])
@AutoConfigureMockMvc(addFilters = false)
class MetadataEntityControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var metadataService: MetadataService

    @MockitoBean
    private lateinit var metadataEditService: MetadataEditService

    @MockitoBean
    private lateinit var metadataRepository: MetadataRepository

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

    @BeforeEach
    fun authenticateWriter() {
        val auth = UsernamePasswordAuthenticationToken("test-user", null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
        whenever(metadataRepository.listFacetInstanceRows(any())).thenReturn(emptyList())
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

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
                jsonPath("$[0].facetType") {
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

        mockMvc.get("/api/v1/metadata/entities/myschema/facets/descriptive")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload") { isNotEmpty() }
            }
    }

    @Test
    fun shouldApplyContext_whenContextParamProvided() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))

        mockMvc.get("/api/v1/metadata/entities/myschema/facets/descriptive") {
            param("context", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$.payload") { isNotEmpty() }
        }
    }

    @Test
    fun shouldReturnBadRequest_whenContextParamIsMalformed() {
        mockMvc.get("/api/v1/metadata/entities/myschema/facets/descriptive") {
            param("context", ",")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value("BAD_REQUEST") }
        }
    }

    // --- Write / history paths (WI-098) ---

    @Test
    fun shouldReturn401_whenCreateEntityWithoutAuthentication() {
        SecurityContextHolder.clearContext()
        try {
            mockMvc.post("/api/v1/metadata/entities") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"id":"x","type":"SCHEMA","schemaName":"x","facets":{}}"""
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.status") { value("UNAUTHORIZED") }
                jsonPath("$.message") { exists() }
            }
        } finally {
            authenticateWriter()
        }
    }

    @Test
    fun shouldCreateEntity_whenPost() {
        whenever(metadataEditService.createEntity(any(), any())).thenAnswer { inv ->
            inv.getArgument(0) as MetadataEntity
        }

        mockMvc.post("/api/v1/metadata/entities") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":"new-entity","type":"SCHEMA","schemaName":"new-entity","facets":{}}"""
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("$.id") { value("new-entity") }
        }
    }

    @Test
    fun shouldOverwriteEntity_whenPut() {
        val updated = MetadataEntity(
            id = "myschema",
            type = MetadataType.SCHEMA,
            schemaName = "myschema",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2025-06-01T00:00:00Z"),
            facets = mutableMapOf()
        )
        whenever(metadataEditService.overwriteEntity(any(), any(), any())).thenReturn(updated)

        mockMvc.put("/api/v1/metadata/entities/myschema") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":"myschema","type":"SCHEMA","schemaName":"myschema","facets":{}}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value("myschema") }
        }
    }

    @Test
    fun shouldOverwriteEntity_whenPatch() {
        val updated = MetadataEntity(
            id = "myschema",
            type = MetadataType.SCHEMA,
            schemaName = "myschema",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2025-06-01T00:00:00Z"),
            facets = mutableMapOf()
        )
        whenever(metadataEditService.overwriteEntity(any(), any(), any())).thenReturn(updated)

        mockMvc.patch("/api/v1/metadata/entities/myschema") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":"myschema","type":"SCHEMA","schemaName":"myschema","facets":{}}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value("myschema") }
        }
    }

    @Test
    fun shouldDeleteEntity_whenDelete() {
        doNothing().whenever(metadataEditService).deleteEntity(any(), any())

        mockMvc.delete("/api/v1/metadata/entities/myschema")
            .andExpect {
                status { isNoContent() }
            }
    }

    @Test
    fun shouldSetFacet_whenPutFacet() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))
        val updated = MetadataEntity(
            id = "myschema",
            type = MetadataType.SCHEMA,
            schemaName = "myschema",
            createdAt = baseEntity.createdAt,
            updatedAt = Instant.parse("2025-06-01T00:00:00Z"),
            facets = mutableMapOf(
                MetadataUrns.FACET_TYPE_DESCRIPTIVE to mutableMapOf(
                    MetadataUrns.SCOPE_GLOBAL to mapOf("displayName" to "Edited")
                )
            )
        )
        whenever(metadataEditService.setFacet(any(), any(), any(), any(), any())).thenReturn(updated)

        mockMvc.put("/api/v1/metadata/entities/myschema/facets/descriptive") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"displayName":"Edited"}"""
            param("context", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$.facetType") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
            jsonPath("$.payload.displayName") { value("Edited") }
        }
    }

    @Test
    fun shouldDeleteFacet_whenDeleteFacet() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))
        doNothing().whenever(metadataEditService).deleteFacet(any(), any(), any(), any())

        mockMvc.delete("/api/v1/metadata/entities/myschema/facets/descriptive") {
            param("context", "global")
        }.andExpect {
            status { isNoContent() }
        }

        verify(metadataEditService).deleteFacet(
            eq("myschema"),
            eq("descriptive"),
            eq(MetadataUrns.SCOPE_GLOBAL),
            eq("test-user")
        )
    }

    @Test
    fun shouldDeleteFacetFromEffectiveScope_whenLastContextScopeHasNoPayload() {
        val entity = MetadataEntity(
            id = "myschema",
            type = MetadataType.SCHEMA,
            schemaName = "myschema",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2025-01-01T00:00:00Z"),
            facets = mutableMapOf(
                MetadataUrns.FACET_TYPE_DESCRIPTIVE to mutableMapOf(
                    MetadataUrns.SCOPE_GLOBAL to mapOf("displayName" to "Only global")
                )
            )
        )
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(entity))
        doNothing().whenever(metadataEditService).deleteFacet(any(), any(), any(), any())

        mockMvc.delete("/api/v1/metadata/entities/myschema/facets/descriptive") {
            param("context", "global,user:alice")
        }.andExpect {
            status { isNoContent() }
        }

        verify(metadataEditService).deleteFacet(
            eq("myschema"),
            eq("descriptive"),
            eq(MetadataUrns.SCOPE_GLOBAL),
            eq("test-user")
        )
    }

    @Test
    fun shouldGetEntityFacetsFromInstanceRows_whenJpaStorage() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))
        val uid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
        whenever(metadataRepository.listFacetInstanceRows("myschema")).thenReturn(
            listOf(
                MetadataFacetInstanceRow(
                    facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                    scopeKey = MetadataUrns.SCOPE_GLOBAL,
                    facetUid = uid,
                    sortKey = 1L,
                    payload = mapOf("displayName" to "My Schema")
                )
            )
        )

        mockMvc.get("/api/v1/metadata/entities/myschema/facets")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].uid") { value(uid) }
                jsonPath("$[0].facetType") { value(MetadataUrns.FACET_TYPE_DESCRIPTIVE) }
            }
    }

    @Test
    fun shouldReturn400_whenDeleteMultipleFacetWithoutUid() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))
        whenever(metadataRepository.listFacetInstanceRows("myschema")).thenReturn(
            listOf(
                MetadataFacetInstanceRow(
                    facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                    scopeKey = MetadataUrns.SCOPE_GLOBAL,
                    facetUid = "u1",
                    sortKey = 1L,
                    payload = mapOf("a" to 1)
                ),
                MetadataFacetInstanceRow(
                    facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                    scopeKey = MetadataUrns.SCOPE_GLOBAL,
                    facetUid = "u2",
                    sortKey = 2L,
                    payload = mapOf("b" to 2)
                )
            )
        )
        whenever(metadataRepository.resolveFacetTargetCardinality(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
            .thenReturn(FacetTargetCardinality.MULTIPLE)
        whenever(
            metadataRepository.countFacetInstancesAtScope(
                eq("myschema"),
                eq(MetadataUrns.FACET_TYPE_DESCRIPTIVE),
                eq(MetadataUrns.SCOPE_GLOBAL)
            )
        ).thenReturn(2)

        mockMvc.delete("/api/v1/metadata/entities/myschema/facets/descriptive") {
            param("context", "global")
        }.andExpect {
            status { isBadRequest() }
        }
        verify(metadataEditService, never()).deleteFacet(any(), any(), any(), any())
        verify(metadataEditService, never()).deleteFacetInstanceByUid(any(), any(), any())
    }

    @Test
    fun shouldDeleteFacetInstance_whenUidQueryParam() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))
        whenever(metadataRepository.listFacetInstanceRows("myschema")).thenReturn(
            listOf(
                MetadataFacetInstanceRow(
                    facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                    scopeKey = MetadataUrns.SCOPE_GLOBAL,
                    facetUid = "u1",
                    sortKey = 1L,
                    payload = emptyMap<String, Any>()
                ),
                MetadataFacetInstanceRow(
                    facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                    scopeKey = MetadataUrns.SCOPE_GLOBAL,
                    facetUid = "u2",
                    sortKey = 2L,
                    payload = emptyMap<String, Any>()
                )
            )
        )
        whenever(metadataRepository.resolveFacetTargetCardinality(any())).thenReturn(FacetTargetCardinality.MULTIPLE)
        whenever(metadataRepository.countFacetInstancesAtScope(any(), any(), any())).thenReturn(2)
        whenever(metadataRepository.findFacetInstanceRow(eq("myschema"), eq("u1"))).thenReturn(
            MetadataFacetInstanceRow(
                facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                scopeKey = MetadataUrns.SCOPE_GLOBAL,
                facetUid = "u1",
                sortKey = 1L,
                payload = emptyMap<String, Any>()
            )
        )
        whenever(metadataEditService.deleteFacetInstanceByUid(any(), any(), any())).thenReturn(baseEntity)

        mockMvc.delete("/api/v1/metadata/entities/myschema/facets/descriptive") {
            param("context", "global")
            param("uid", "u1")
        }.andExpect {
            status { isNoContent() }
        }

        verify(metadataEditService).deleteFacetInstanceByUid(eq("myschema"), eq("u1"), eq("test-user"))
    }

    @Test
    fun shouldDeleteFacetInstance_whenPathSegment() {
        whenever(metadataRepository.findFacetInstanceRow(eq("myschema"), eq("facet-uid-1"))).thenReturn(
            MetadataFacetInstanceRow(
                facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                scopeKey = MetadataUrns.SCOPE_GLOBAL,
                facetUid = "facet-uid-1",
                sortKey = 1L,
                payload = emptyMap<String, Any>()
            )
        )
        whenever(metadataEditService.deleteFacetInstanceByUid(any(), any(), any())).thenReturn(baseEntity)

        mockMvc.delete("/api/v1/metadata/entities/myschema/facet-instances/facet-uid-1")
            .andExpect {
                status { isNoContent() }
            }

        verify(metadataEditService).deleteFacetInstanceByUid(eq("myschema"), eq("facet-uid-1"), eq("test-user"))
    }

    @Test
    fun shouldReturnHistory_whenGetHistory() {
        val occurred = Instant.parse("2025-01-15T12:00:00Z")
        val row = MetadataOperationAuditRecord(
            auditId = "audit-1",
            operationType = "ENTITY_UPDATED",
            entityId = "myschema",
            facetType = null,
            scopeKey = null,
            actorId = "test-user",
            occurredAt = occurred,
            payloadBefore = null,
            payloadAfter = null,
            changeSummary = null
        )
        whenever(metadataEditService.history("myschema")).thenReturn(listOf(row))

        mockMvc.get("/api/v1/metadata/entities/myschema/history")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(1) }
                jsonPath("$[0].auditId") { value("audit-1") }
                jsonPath("$[0].operationType") { value("ENTITY_UPDATED") }
                jsonPath("$[0].entityId") { value("myschema") }
            }
    }

    @Test
    fun shouldReturn404WithMillStatusBody_whenOverwriteEntityNotFound() {
        whenever(metadataEditService.overwriteEntity(any(), any(), any())).thenThrow(
            MillStatuses.notFoundRuntime("Entity not found: myschema")
        )

        mockMvc.put("/api/v1/metadata/entities/myschema") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":"myschema","type":"SCHEMA","schemaName":"myschema","facets":{}}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.status") { value("NOT_FOUND") }
            jsonPath("$.message") { exists() }
        }
    }

    @Test
    fun shouldReturn422WithMillStatusBody_whenFacetPayloadMissingAfterWrite() {
        whenever(metadataService.findById("myschema")).thenReturn(Optional.of(baseEntity))
        val missingPayloadFacet = MetadataEntity(
            id = "myschema",
            type = MetadataType.SCHEMA,
            schemaName = "myschema",
            facets = mutableMapOf()
        )
        whenever(metadataEditService.setFacet(any(), any(), any(), any(), any())).thenReturn(missingPayloadFacet)

        mockMvc.put("/api/v1/metadata/entities/myschema/facets/descriptive") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            param("context", "global")
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$.status") { value("UNPROCESSABLE") }
            jsonPath("$.message") { exists() }
        }
    }
}
