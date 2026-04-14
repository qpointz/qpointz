package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.profile.ProfileRegistry
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [AiProfileController::class])
@Import(AiChatExceptionHandler::class)
class AiProfileControllerTest {

    @Autowired
    private lateinit var client: WebTestClient

    @MockitoBean
    private lateinit var profileRegistry: ProfileRegistry

    private val sampleProfile = AgentProfile(
        id = "hello-world",
        capabilityIds = setOf("conversation", "demo"),
    )

    @Test
    fun `listProfiles should return 200 with profile array`() {
        whenever(profileRegistry.registeredProfiles()).thenReturn(listOf(sampleProfile))

        client.get().uri("/api/v1/ai/profiles")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].id").isEqualTo("hello-world")
            .jsonPath("$[0].capabilityIds[0]").isEqualTo("conversation")
            .jsonPath("$[0].capabilityIds[1]").isEqualTo("demo")
    }

    @Test
    fun `getProfile should return 200 when profile exists`() {
        whenever(profileRegistry.resolve("hello-world")).thenReturn(sampleProfile)

        client.get().uri("/api/v1/ai/profiles/hello-world")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("hello-world")
    }

    @Test
    fun `getProfile should return 404 when profile missing`() {
        whenever(profileRegistry.resolve("missing")).thenReturn(null)

        client.get().uri("/api/v1/ai/profiles/missing")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
}
