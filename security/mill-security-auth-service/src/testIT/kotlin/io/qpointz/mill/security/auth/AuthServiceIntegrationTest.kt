package io.qpointz.mill.security.auth

import io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord
import io.qpointz.mill.persistence.security.jpa.hasher.NoOpPasswordHasher
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.service.JpaUserIdentityResolutionService
import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.ErrorResponse
import io.qpointz.mill.security.auth.dto.LoginRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Integration tests for the auth service REST endpoints.
 *
 * Spring Boot 4 removed `TestRestTemplate`. These ITs use [WebTestClient] against a real
 * random-port server to validate MVC endpoint behaviour, session cookie handling, and JSON errors.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthServiceIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var identityResolutionService: JpaUserIdentityResolutionService

    @Autowired
    private lateinit var credentialRepository: UserCredentialRepository

    private val hasher = NoOpPasswordHasher()

    private fun baseUrl() = "http://localhost:$port"

    private fun client(baseUrl: String = baseUrl()): WebTestClient =
        WebTestClient.bindToServer()
            .baseUrl(baseUrl)
            .responseTimeout(Duration.ofSeconds(10))
            .build()

    @BeforeEach
    fun seedTestUser() {
        val resolved = identityResolutionService.resolveOrProvision(
            provider = "local",
            subject = "testuser",
            displayName = "Test User",
            email = "testuser@example.com",
        )

        val existing = credentialRepository.findByUserIdAndEnabledTrue(resolved.userId)
        if (existing == null) {
            credentialRepository.save(
                UserCredentialRecord(
                    credentialId = UUID.randomUUID().toString(),
                    userId = resolved.userId,
                    passwordHash = hasher.hash("testpass"),
                    algorithm = hasher.algorithmId,
                    enabled = true,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
            )
        }
    }

    @Test
    fun `POST auth-public-login with correct credentials returns 200 with session cookie and AuthMeResponse`() {
        val loginRequest = LoginRequest("testuser", "testpass")

        val result = client()
            .post()
            .uri("/auth/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthMeResponse::class.java)
            .returnResult()

        val body = result.responseBody!!
        assertThat(body.securityEnabled).isTrue()
        assertThat(body.userId).isNotBlank()
        assertThat(body.userId).isNotEqualTo("anonymous")

        val session = result.responseCookies["JSESSIONID"]?.firstOrNull()?.value
        assertThat(session).isNotBlank()
    }

    @Test
    fun `POST auth-public-login with wrong credentials returns 401 JSON error`() {
        val loginRequest = LoginRequest("testuser", "wrongpassword")

        val result = client()
            .post()
            .uri("/auth/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java)
            .returnResult()

        val body = result.responseBody!!
        assertThat(body.status).isEqualTo(401)
        assertThat(body.error).isEqualTo("Unauthorized")
    }

    @Test
    fun `GET auth-me after successful login with session cookie returns 200 AuthMeResponse`() {
        val loginRequest = LoginRequest("testuser", "testpass")

        val loginResult = client()
            .post()
            .uri("/auth/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthMeResponse::class.java)
            .returnResult()

        val session = loginResult.responseCookies["JSESSIONID"]?.firstOrNull()?.value
        assertThat(session).isNotBlank()

        val meBody = client()
            .get()
            .uri("/auth/me")
            .cookie("JSESSIONID", session!!)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthMeResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(meBody.securityEnabled).isTrue()
        assertThat(meBody.userId).isNotBlank()
    }

    @Test
    fun `POST auth-logout after login returns 200 and subsequent GET auth-me with old session returns 401`() {
        val loginRequest = LoginRequest("testuser", "testpass")

        val loginResult = client()
            .post()
            .uri("/auth/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthMeResponse::class.java)
            .returnResult()

        val session = loginResult.responseCookies["JSESSIONID"]?.firstOrNull()?.value
        assertThat(session).isNotBlank()

        client()
            .post()
            .uri("/auth/logout")
            .cookie("JSESSIONID", session!!)
            .exchange()
            .expectStatus().isOk

        client()
            .get()
            .uri("/auth/me")
            .cookie("JSESSIONID", session)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `auth-public-login is accessible without any prior session (Order(-6) fires before Order(0) chain)`() {
        val loginRequest = LoginRequest("testuser", "testpass")

        client()
            .post()
            .uri("/auth/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(properties = ["mill.security.enable=false"])
    inner class SecurityOffMode {

        @LocalServerPort
        private var secOffPort: Int = 0

        @Test
        fun `GET auth-me when security off returns 200 AuthMeResponse with securityEnabled false`() {
            val result = client("http://localhost:$secOffPort")
                .get()
                .uri("/auth/me")
                .exchange()
                .expectStatus().isOk
                .expectBody(AuthMeResponse::class.java)
                .returnResult()

            val body = result.responseBody!!
            assertThat(body.securityEnabled).isFalse()
            assertThat(body.userId).isEqualTo("anonymous")
        }
    }
}
