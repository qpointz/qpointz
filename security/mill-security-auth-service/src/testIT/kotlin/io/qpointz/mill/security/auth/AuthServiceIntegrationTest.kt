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
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.time.Instant
import java.util.UUID

/**
 * Integration tests for the auth service REST endpoints.
 *
 * Verifies end-to-end HTTP behaviour of `POST /auth/public/login`, `GET /auth/me`,
 * and `POST /auth/logout` including session cookie handling. A real H2 in-memory
 * database is used; no mocks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthServiceIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var identityResolutionService: JpaUserIdentityResolutionService

    @Autowired
    private lateinit var credentialRepository: UserCredentialRepository

    private val hasher = NoOpPasswordHasher()

    private fun baseUrl() = "http://localhost:$port"

    @BeforeEach
    fun seedTestUser() {
        // Provision the test user identity
        val resolved = identityResolutionService.resolveOrProvision(
            provider = "local",
            subject = "testuser",
            displayName = "Test User",
            email = "testuser@example.com",
        )

        // Seed credential if not already present
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
                )
            )
        }
    }

    @Test
    fun `POST auth-public-login with correct credentials returns 200 with session cookie and AuthMeResponse`() {
        val loginRequest = LoginRequest("testuser", "testpass")

        val response = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/login",
            loginRequest,
            AuthMeResponse::class.java,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body.securityEnabled).isTrue()
        assertThat(body.userId).isNotBlank()
        assertThat(body.userId).isNotEqualTo("anonymous")
        assertThat(response.headers["Set-Cookie"]).isNotNull
    }

    @Test
    fun `POST auth-public-login with wrong credentials returns 401 JSON error`() {
        val loginRequest = LoginRequest("testuser", "wrongpassword")

        val response = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/login",
            loginRequest,
            ErrorResponse::class.java,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        val body = response.body!!
        assertThat(body.status).isEqualTo(401)
        assertThat(body.error).isEqualTo("Unauthorized")
        // Must not redirect — response body must be JSON, not HTML
        assertThat(response.headers.contentType?.toString()).contains("application/json")
    }

    @Test
    fun `GET auth-me after successful login with session cookie returns 200 AuthMeResponse`() {
        // Login to get session
        val loginRequest = LoginRequest("testuser", "testpass")
        val loginResponse = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/login",
            loginRequest,
            AuthMeResponse::class.java,
        )
        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.OK)

        val setCookieHeader = loginResponse.headers["Set-Cookie"]
        assertThat(setCookieHeader).isNotNull
        val sessionCookie = setCookieHeader!!.firstOrNull { it.startsWith("JSESSIONID") }
        assertThat(sessionCookie).isNotNull

        // Use the session to call /auth/me
        val headers = HttpHeaders()
        headers["Cookie"] = sessionCookie
        val meResponse = restTemplate.exchange(
            "${baseUrl()}/auth/me",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            AuthMeResponse::class.java,
        )

        assertThat(meResponse.statusCode).isEqualTo(HttpStatus.OK)
        val body = meResponse.body!!
        assertThat(body.securityEnabled).isTrue()
        assertThat(body.userId).isNotBlank()
    }

    @Test
    fun `POST auth-logout after login returns 200 and subsequent GET auth-me with old session returns 401`() {
        // Login
        val loginRequest = LoginRequest("testuser", "testpass")
        val loginResponse = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/login",
            loginRequest,
            AuthMeResponse::class.java,
        )
        val setCookieHeader = loginResponse.headers["Set-Cookie"]
        assertThat(setCookieHeader).isNotNull
        val sessionCookie = setCookieHeader!!.firstOrNull { it.startsWith("JSESSIONID") }!!

        val cookieHeaders = HttpHeaders()
        cookieHeaders["Cookie"] = sessionCookie

        // Logout
        val logoutResponse = restTemplate.exchange(
            "${baseUrl()}/auth/logout",
            HttpMethod.POST,
            HttpEntity<Void>(cookieHeaders),
            Void::class.java,
        )
        assertThat(logoutResponse.statusCode).isEqualTo(HttpStatus.OK)

        // Subsequent /auth/me with old session should return 401
        val meResponse = restTemplate.exchange(
            "${baseUrl()}/auth/me",
            HttpMethod.GET,
            HttpEntity<Void>(cookieHeaders),
            Void::class.java,
        )
        assertThat(meResponse.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `auth-public-login is accessible without any prior session (Order(-6) fires before Order(0) chain)`() {
        // No session — fresh request with no cookies
        val loginRequest = LoginRequest("testuser", "testpass")
        val response = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/login",
            loginRequest,
            AuthMeResponse::class.java,
        )

        // Must not get 401 or redirect — the public chain at @Order(-6) must allow it
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    /**
     * Nested test class for security-off mode.
     *
     * Overrides `mill.security.enable=false` so that both controllers return anonymous
     * responses without touching the [AuthenticationManager].
     */
    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(properties = ["mill.security.enable=false"])
    inner class SecurityOffMode {

        @LocalServerPort
        private var secOffPort: Int = 0

        @Autowired
        private lateinit var secOffRestTemplate: TestRestTemplate

        @Test
        fun `GET auth-me when security off returns 200 AuthMeResponse with securityEnabled false`() {
            val response = secOffRestTemplate.getForEntity(
                "http://localhost:$secOffPort/auth/me",
                AuthMeResponse::class.java,
            )

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            val body = response.body!!
            assertThat(body.securityEnabled).isFalse()
            assertThat(body.userId).isEqualTo("anonymous")
        }
    }
}
