package io.qpointz.mill.security.auth

import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.ErrorResponse
import io.qpointz.mill.security.auth.dto.RegisterRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

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

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private fun baseUrl() = "http://localhost:$port"

    @Test
    fun `POST auth-public-register with valid new user returns 201 and session cookie`() {
        val registerRequest = RegisterRequest(
            email = "newuser@example.com",
            password = "password123",
            displayName = "New User",
        )

        val response = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/register",
            registerRequest,
            AuthMeResponse::class.java,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val body = response.body!!
        assertThat(body.userId).isNotBlank()
        assertThat(body.userId).isNotEqualTo("anonymous")
        assertThat(body.email).isEqualTo("newuser@example.com")
        assertThat(body.securityEnabled).isTrue()
    }

    @Test
    fun `POST auth-public-register with duplicate email returns 409`() {
        val registerRequest = RegisterRequest(
            email = "duplicate@example.com",
            password = "password123",
            displayName = "First User",
        )

        // First registration — should succeed
        val firstResponse = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/register",
            registerRequest,
            AuthMeResponse::class.java,
        )
        assertThat(firstResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        // Second registration with same email — should conflict
        val secondResponse = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/register",
            registerRequest,
            ErrorResponse::class.java,
        )

        assertThat(secondResponse.statusCode).isEqualTo(HttpStatus.CONFLICT)
        val body = secondResponse.body!!
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

        val response = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/register",
            registerRequest,
            ErrorResponse::class.java,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        val body = response.body!!
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

        @Autowired
        private lateinit var disabledRestTemplate: TestRestTemplate

        @Test
        fun `POST auth-public-register when disabled returns 403`() {
            val registerRequest = RegisterRequest(
                email = "someone@example.com",
                password = "password123",
                displayName = null,
            )

            val response = disabledRestTemplate.postForEntity(
                "http://localhost:$disabledPort/auth/public/register",
                registerRequest,
                ErrorResponse::class.java,
            )

            assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            val body = response.body!!
            assertThat(body.status).isEqualTo(403)
            assertThat(body.error).isEqualTo("Forbidden")
        }
    }
}
