package io.qpointz.mill.security.auth

import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.ErrorResponse
import io.qpointz.mill.security.auth.dto.RegisterRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

/**
 * Integration tests for `POST /auth/public/register`.
 *
 * All scenarios use an H2 in-memory database via the test application context defined in
 * [TestAuthServiceApplication]. Registration tests that require the feature enabled use a
 * `@TestPropertySource` override to set `mill.security.allow-registration=true`.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["mill.security.allow-registration=true", "mill.security.enable=true"],
)
class RegistrationIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    private fun baseUrl() = "http://localhost:$port"

    private fun client(baseUrl: String = baseUrl()): WebTestClient =
        WebTestClient.bindToServer()
            .baseUrl(baseUrl)
            .responseTimeout(Duration.ofSeconds(10))
            .build()

    @Test
    fun `POST auth-public-register with valid new user returns 201 and session cookie`() {
        val registerRequest = RegisterRequest(
            email = "newuser@example.com",
            password = "password123",
            displayName = "New User",
        )

        val result = client()
            .post()
            .uri("/auth/public/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(AuthMeResponse::class.java)
            .returnResult()

        val body = result.responseBody!!
        assertThat(body.userId).isNotBlank()
        assertThat(body.userId).isNotEqualTo("anonymous")
        assertThat(body.email).isEqualTo("newuser@example.com")
        assertThat(body.securityEnabled).isTrue()
        val session = result.responseCookies["JSESSIONID"]?.firstOrNull()?.value
        assertThat(session).isNotBlank()
    }

    @Test
    fun `POST auth-public-register with duplicate email returns 409`() {
        val registerRequest = RegisterRequest(
            email = "duplicate@example.com",
            password = "password123",
            displayName = "First User",
        )

        // First registration — should succeed
        client()
            .post()
            .uri("/auth/public/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isCreated

        // Second registration with same email — should conflict
        val result = client()
            .post()
            .uri("/auth/public/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java)
            .returnResult()

        val body = result.responseBody!!
        assertThat(body.status).isEqualTo(409)
        assertThat(body.error).isEqualTo("Conflict")
    }

    @Test
    fun `POST auth-public-register with invalid email returns 422`() {
        val registerRequest = RegisterRequest(
            email = "not-an-email",
            password = "password123",
            displayName = null,
        )

        val result = client()
            .post()
            .uri("/auth/public/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java)
            .returnResult()

        val body = result.responseBody!!
        assertThat(body.status).isEqualTo(422)
    }

    /**
     * Nested test class for registration-disabled mode.
     */
    @Nested
    @SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["mill.security.allow-registration=false", "mill.security.enable=true"],
    )
    inner class RegistrationDisabled {

        @LocalServerPort
        private var disabledPort: Int = 0

        @Test
        fun `POST auth-public-register when disabled returns 403`() {
            val registerRequest = RegisterRequest(
                email = "someone@example.com",
                password = "password123",
                displayName = null,
            )

            val result = client("http://localhost:$disabledPort")
                .post()
                .uri("/auth/public/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isForbidden
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(ErrorResponse::class.java)
                .returnResult()

            val body = result.responseBody!!
            assertThat(body.status).isEqualTo(403)
            assertThat(body.error).isEqualTo("Forbidden")
        }
    }
}
