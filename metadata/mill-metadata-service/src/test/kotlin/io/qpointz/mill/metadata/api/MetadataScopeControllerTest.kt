package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.service.MetadataScopeService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
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
import java.time.Instant

@WebMvcTest(controllers = [MetadataScopeController::class, MetadataExceptionHandler::class])
@AutoConfigureMockMvc(addFilters = false)
class MetadataScopeControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var scopeService: MetadataScopeService

    private val now = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun `shouldReturnScopeList_whenGetScopes`() {
        whenever(scopeService.findAll()).thenReturn(listOf(
            MetadataScope(scopeId = MetadataUrns.SCOPE_GLOBAL, displayName = "Global", ownerId = null, createdAt = now),
            MetadataScope(scopeId = MetadataUrns.scopeUser("alice"), displayName = null, ownerId = "alice", createdAt = now)
        ))

        mvc.get("/api/v1/metadata/scopes")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].scopeId") { value(MetadataUrns.SCOPE_GLOBAL) }
                jsonPath("$[1].scopeId") { value(MetadataUrns.scopeUser("alice")) }
            }
    }

    @Test
    fun `shouldReturnCreated_whenPostScope`() {
        val userScope = MetadataUrns.scopeUser("newuser")
        whenever(scopeService.create(userScope, "New User Scope", null))
            .thenReturn(MetadataScope(scopeId = userScope, displayName = "New User Scope", ownerId = null, createdAt = now))

        mvc.post("/api/v1/metadata/scopes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"scopeId":"$userScope","displayName":"New User Scope"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.scopeId") { value(userScope) }
            header { exists("Location") }
        }
    }

    @Test
    fun `shouldReturn409_whenPostDuplicateScope`() {
        doThrow(IllegalArgumentException("Scope already exists")).whenever(scopeService).create(anyOrNull(), anyOrNull(), anyOrNull())

        mvc.post("/api/v1/metadata/scopes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"scopeId":"${MetadataUrns.SCOPE_GLOBAL}"}"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `shouldReturnNoContent_whenDeleteUserScope`() {
        mvc.delete("/api/v1/metadata/scopes/user:alice")
            .andExpect {
                status { isNoContent() }
            }
    }

    @Test
    fun `shouldReturn409_whenDeleteGlobalScope`() {
        doThrow(IllegalArgumentException("Cannot delete the global scope")).whenever(scopeService).delete(MetadataUrns.SCOPE_GLOBAL)

        mvc.delete("/api/v1/metadata/scopes/global")
            .andExpect {
                status { isConflict() }
            }
    }
}
