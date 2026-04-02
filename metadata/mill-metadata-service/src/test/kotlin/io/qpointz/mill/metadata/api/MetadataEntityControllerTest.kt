package io.qpointz.mill.metadata.api

import io.qpointz.mill.UrnSlug
import io.qpointz.mill.data.metadata.CatalogPath
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetOrigin
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.domain.facet.toCapturedReadModel
import io.qpointz.mill.metadata.repository.FacetReadSide
import io.qpointz.mill.metadata.service.FacetService
import io.qpointz.mill.metadata.service.MetadataEditService
import io.qpointz.mill.metadata.service.MetadataReader
import io.qpointz.mill.metadata.service.MetadataService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete as servletDelete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get as servletGet
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post as servletPost
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch as servletPatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put as servletPut
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.util.UriUtils
import java.net.URI
import java.nio.charset.StandardCharsets
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
    private lateinit var facetReadSide: FacetReadSide

    @MockitoBean
    private lateinit var facetService: FacetService

    @MockitoBean
    private lateinit var metadataReader: MetadataReader

    @MockitoBean
    private lateinit var urnCodec: MetadataEntityUrnCodec

    private val schemaUrn = "urn:mill/metadata/entity:myschema"

    /**
     * Builds a request URI with the entity id as **one** encoded path segment (URNs contain `/`).
     * Uses [URI.create] so MockMvc does not re-encode `%` (avoids `%252F` double encoding on plain string URLs).
     */
    private fun entityUri(entityId: String, suffix: String = ""): URI {
        val path = "/api/v1/metadata/entities/" +
            UriUtils.encodePathSegment(entityId, StandardCharsets.UTF_8) +
            suffix
        return URI.create("http://localhost$path")
    }

    /** Slug segment (no `/`) — matches UI + [UrnSlug.encode]. */
    private fun entitySlugUri(entityId: String, suffix: String = ""): URI {
        val slug = UrnSlug.encode(entityId)
        val path = "/api/v1/metadata/entities/" +
            UriUtils.encodePathSegment(slug, StandardCharsets.UTF_8) +
            suffix
        return URI.create("http://localhost$path")
    }

    private val baseEntity = MetadataEntity(
        id = schemaUrn,
        kind = "schema",
        uuid = "550e8400-e29b-41d4-a716-446655440000",
        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        createdBy = "system",
        lastModifiedAt = Instant.parse("2025-01-01T00:00:00Z"),
        lastModifiedBy = "system"
    )

    private val descriptiveAssignment = FacetAssignment(
        uid = "facet-uid-1",
        entityId = schemaUrn,
        facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
        scopeKey = MetadataUrns.SCOPE_GLOBAL,
        mergeAction = MergeAction.SET,
        payload = mapOf("displayName" to "My Schema"),
        createdAt = Instant.EPOCH,
        createdBy = "u",
        lastModifiedAt = Instant.EPOCH,
        lastModifiedBy = "u"
    )

    private val descriptiveRead = descriptiveAssignment.toCapturedReadModel()

    @BeforeEach
    fun setupStubs() {
        val auth = UsernamePasswordAuthenticationToken("test-user", null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
        whenever(urnCodec.decode(schemaUrn)).thenReturn(CatalogPath("myschema", null, null))
        whenever(facetReadSide.findByEntity(schemaUrn)).thenReturn(listOf(descriptiveAssignment))
        whenever(facetReadSide.findByEntityAndType(eq(schemaUrn), any())).thenReturn(listOf(descriptiveAssignment))
        whenever(facetService.resolve(eq(schemaUrn), any())).thenReturn(listOf(descriptiveRead))
        whenever(facetService.resolveByType(eq(schemaUrn), any(), any())).thenReturn(listOf(descriptiveRead))
        whenever(metadataReader.resolveEffective(any(), any())).thenReturn(listOf(descriptiveAssignment))
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
                jsonPath("$[0].entityUrn") { value(schemaUrn) }
                jsonPath("$[0].kind") { value("schema") }
            }
    }

    @Test
    fun shouldReturnBadRequest_whenListWithSchemaQueryParam() {
        mockMvc.get("/api/v1/metadata/entities") {
            param("schema", "myschema")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun shouldListByKind_whenKindParamProvided() {
        whenever(metadataService.findByKind("table")).thenReturn(emptyList())

        mockMvc.get("/api/v1/metadata/entities") {
            param("kind", "table")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun shouldGetEntityById_whenEntityExists() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))

        mockMvc.perform(servletGet(entityUri(schemaUrn)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.entityUrn").value(schemaUrn))
            .andExpect(jsonPath("$.kind").value("schema"))
    }

    @Test
    fun shouldReturn400_whenEntityPathIsDotKey() {
        mockMvc.get("/api/v1/metadata/entities/myschema")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun shouldReturn404_whenEntityNotFound() {
        val missing = "urn:mill/metadata/entity:missing"
        whenever(metadataService.findById(missing)).thenReturn(Optional.empty())

        mockMvc.perform(servletGet(entityUri(missing)))
            .andExpect(status().isNotFound)
    }

    @Test
    fun shouldGetEntityFacets_whenEntityExists() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))

        mockMvc.perform(servletGet(entityUri(schemaUrn, "/facets")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].facetTypeUrn").value(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
            .andExpect(jsonPath("$[0].uid").value("facet-uid-1"))
            .andExpect(jsonPath("$[0].origin").value(FacetOrigin.CAPTURED.name))
            .andExpect(jsonPath("$[0].assignmentUid").value("facet-uid-1"))
    }

    @Test
    fun shouldGetEntityFacets_whenEntityIdIsUrnSlugPathSegment() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))

        mockMvc.perform(servletGet(entitySlugUri(schemaUrn, "/facets")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].facetTypeUrn").value(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
            .andExpect(jsonPath("$[0].uid").value("facet-uid-1"))
    }

    @Test
    fun shouldReturnEmptyFacets_whenNoMetadataEntityRow() {
        val missing = "urn:mill/metadata/entity:nope"
        whenever(metadataService.findById(missing)).thenReturn(Optional.empty())
        whenever(facetService.resolve(eq(missing), any())).thenReturn(emptyList())

        mockMvc.perform(servletGet(entityUri(missing, "/facets")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun shouldGetEntityFacets_whenPathIdIsUrnSlug() {
        val tableUrn = "urn:mill/model/table:myschema.mytable"
        whenever(facetService.resolve(eq(tableUrn), any())).thenReturn(listOf(descriptiveRead))

        mockMvc.perform(servletGet(entitySlugUri(tableUrn, "/facets")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].uid").value("facet-uid-1"))
    }

    @Test
    fun shouldGetFacetByType_whenEntityAndFacetExist() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))

        mockMvc.perform(servletGet(entityUri(schemaUrn, "/facets/descriptive")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].facetTypeUrn").value(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
    }

    @Test
    fun shouldReturnEmptyArrayForFacetByType_whenFacetTypeAbsent() {
        whenever(facetService.resolveByType(eq(schemaUrn), any(), any())).thenReturn(emptyList())

        mockMvc.perform(servletGet(entityUri(schemaUrn, "/facets/structural")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun shouldReturnBadRequest_whenScopeParamIsMalformed() {
        mockMvc.perform(
            servletGet(entityUri(schemaUrn, "/facets/descriptive"))
                .param("scope", ",")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
    }

    @Test
    fun shouldAcceptLegacyContext_whenScopeAbsent() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))

        mockMvc.perform(
            servletGet(entityUri(schemaUrn, "/facets/descriptive"))
                .param("context", "global")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].uid").value("facet-uid-1"))
    }

    @Test
    fun shouldReturn401_whenCreateEntityWithoutAuthentication() {
        SecurityContextHolder.clearContext()
        try {
            mockMvc.post("/api/v1/metadata/entities") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"entityUrn":"urn:mill/metadata/entity:x","kind":"schema"}"""
            }.andExpect {
                status { isUnauthorized() }
            }
        } finally {
            val auth = UsernamePasswordAuthenticationToken("test-user", null, emptyList())
            SecurityContextHolder.getContext().authentication = auth
        }
    }

    @Test
    fun shouldCreateEntity_whenPost() {
        val newUrn = "urn:mill/metadata/entity:new-entity"
        whenever(urnCodec.decode(newUrn)).thenReturn(CatalogPath("new-entity", null, null))
        val created = MetadataEntity(
            id = newUrn,
            kind = "schema",
            uuid = "u1",
            createdAt = Instant.now(),
            createdBy = "test-user",
            lastModifiedAt = Instant.now(),
            lastModifiedBy = "test-user"
        )
        whenever(metadataEditService.createEntity(any(), eq("test-user"))).thenReturn(created)
        whenever(metadataService.findById(newUrn)).thenReturn(Optional.of(created))
        whenever(facetReadSide.findByEntity(newUrn)).thenReturn(emptyList())

        mockMvc.post("/api/v1/metadata/entities") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"entityUrn":"$newUrn","kind":"schema"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.entityUrn") { value(newUrn) }
        }
    }

    @Test
    fun shouldOverwriteEntity_whenPut() {
        whenever(metadataEditService.overwriteEntity(any(), any(), any())).thenReturn(baseEntity)
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))
        whenever(facetReadSide.findByEntity(schemaUrn)).thenReturn(emptyList())

        mockMvc.perform(
            servletPut(entityUri(schemaUrn))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"kind":"schema"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.entityUrn").value(schemaUrn))
    }

    @Test
    fun shouldDeleteEntity_whenDelete() {
        doNothing().whenever(metadataEditService).deleteEntity(any(), any())

        mockMvc.perform(servletDelete(entityUri(schemaUrn)))
            .andExpect(status().isNoContent)
    }

    @Test
    fun shouldAssignFacet_whenPostFacet() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))
        whenever(metadataEditService.setFacet(any(), any(), any(), any(), any())).thenReturn(descriptiveRead)

        mockMvc.perform(
            servletPost(entityUri(schemaUrn, "/facets/descriptive"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"displayName":"X"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.facetTypeUrn").value(MetadataUrns.FACET_TYPE_DESCRIPTIVE))
    }

    @Test
    fun shouldPatchFacet_whenPersistedRowExists() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))
        whenever(facetReadSide.findByUid("facet-uid-1")).thenReturn(descriptiveAssignment)
        whenever(facetService.update(eq("facet-uid-1"), any(), eq("test-user"))).thenReturn(descriptiveRead)

        mockMvc.perform(
            servletPatch(entityUri(schemaUrn, "/facets/descriptive/facet-uid-1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"displayName":"Y"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.uid").value("facet-uid-1"))
    }

    @Test
    fun shouldReturn422_whenPatchInferredFacetUid() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))
        whenever(facetReadSide.findByUid("inf-1")).thenReturn(null)
        val inferred = FacetInstance(
            assignmentUuid = "inf-1",
            entityId = schemaUrn,
            facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            scopeKey = MetadataUrns.SCOPE_GLOBAL,
            mergeAction = MergeAction.SET,
            payload = emptyMap(),
            createdAt = Instant.EPOCH,
            createdBy = null,
            lastModifiedAt = Instant.EPOCH,
            lastModifiedBy = null,
            origin = FacetOrigin.INFERRED,
            originId = "logical-layout",
            assignmentUid = null
        )
        whenever(facetService.resolve(eq(schemaUrn), any())).thenReturn(listOf(inferred))

        mockMvc.perform(
            servletPatch(entityUri(schemaUrn, "/facets/descriptive/inf-1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{}""")
        )
            .andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun shouldReturn404_whenPatchUnknownFacetUid() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))
        whenever(facetReadSide.findByUid("missing")).thenReturn(null)
        whenever(facetService.resolve(eq(schemaUrn), any())).thenReturn(emptyList())

        mockMvc.perform(
            servletPatch(entityUri(schemaUrn, "/facets/descriptive/missing"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{}""")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun shouldDeleteFacetAtScope_whenDeleteWithScopeQuery() {
        whenever(metadataService.findById(schemaUrn)).thenReturn(Optional.of(baseEntity))
        whenever(facetReadSide.findByEntity(schemaUrn)).thenReturn(listOf(descriptiveAssignment))
        doNothing().whenever(metadataEditService).deleteFacet(any(), any(), any(), any())

        mockMvc.perform(
            servletDelete(entityUri(schemaUrn, "/facets/descriptive"))
                .param("scope", "global")
        )
            .andExpect(status().isNoContent)
        verify(metadataEditService).deleteFacet(any(), any(), any(), eq("test-user"))
    }
}
