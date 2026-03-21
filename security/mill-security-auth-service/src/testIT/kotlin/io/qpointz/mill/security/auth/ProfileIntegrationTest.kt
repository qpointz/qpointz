package io.qpointz.mill.security.auth

import io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord
import io.qpointz.mill.persistence.security.jpa.hasher.NoOpPasswordHasher
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.service.JpaUserIdentityResolutionService
import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.LoginRequest
import io.qpointz.mill.security.auth.dto.UserProfilePatch
import io.qpointz.mill.security.auth.dto.UserProfileResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID

/**
 * Integration tests for the user profile endpoints.
 *
 * Verifies end-to-end HTTP behaviour of `GET /auth/me` (profile field) and
 * `PATCH /auth/profile` using a real H2 in-memory database. No mocks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfileIntegrationTest {

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
        val resolved = identityResolutionService.resolveOrProvision(
            provider = "local",
            subject = "profileuser",
            displayName = "Profile User",
            email = "profileuser@example.com",
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
                )
            )
        }
    }

    /** Logs in as profileuser and returns the session cookie string (JSESSIONID=...). */
    private fun loginAndGetSessionCookie(): String {
        val loginRequest = LoginRequest("profileuser", "testpass")
        val loginResponse = restTemplate.postForEntity(
            "${baseUrl()}/auth/public/login",
            loginRequest,
            AuthMeResponse::class.java,
        )
        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.OK)
        val setCookieHeader = loginResponse.headers["Set-Cookie"]
        assertThat(setCookieHeader).isNotNull
        val cookie = setCookieHeader!!.firstOrNull { it.startsWith("JSESSIONID") }
        assertThat(cookie).isNotNull
        return cookie!!
    }

    @Test
    fun `GET auth-me after login returns non-null profile auto-created`() {
        val sessionCookie = loginAndGetSessionCookie()
        val headers = HttpHeaders().also { it["Cookie"] = sessionCookie }

        val response = restTemplate.exchange(
            "${baseUrl()}/auth/me",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            AuthMeResponse::class.java,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body.profile).isNotNull
        assertThat(body.profile!!.userId).isNotBlank()
    }

    @Test
    fun `PATCH auth-profile persists displayName reflected in subsequent GET auth-me`() {
        val sessionCookie = loginAndGetSessionCookie()
        val headers = HttpHeaders().also { it["Cookie"] = sessionCookie }

        val patch = UserProfilePatch(displayName = "Alice", email = null, locale = null)
        val patchResponse = restTemplate.exchange(
            "${baseUrl()}/auth/profile",
            HttpMethod.PATCH,
            HttpEntity(patch, headers),
            UserProfileResponse::class.java,
        )

        assertThat(patchResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(patchResponse.body!!.displayName).isEqualTo("Alice")

        // Confirm it is visible in /auth/me
        val meResponse = restTemplate.exchange(
            "${baseUrl()}/auth/me",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            AuthMeResponse::class.java,
        )
        assertThat(meResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(meResponse.body!!.profile!!.displayName).isEqualTo("Alice")
    }

    @Test
    fun `PATCH auth-profile partial update leaves other fields unchanged`() {
        val sessionCookie = loginAndGetSessionCookie()
        val headers = HttpHeaders().also { it["Cookie"] = sessionCookie }

        // Set all three fields first
        restTemplate.exchange(
            "${baseUrl()}/auth/profile",
            HttpMethod.PATCH,
            HttpEntity(UserProfilePatch(displayName = "Bob", email = "bob@test.com", locale = "de"), headers),
            UserProfileResponse::class.java,
        )

        // Only update displayName
        val patchResponse = restTemplate.exchange(
            "${baseUrl()}/auth/profile",
            HttpMethod.PATCH,
            HttpEntity(UserProfilePatch(displayName = "Robert", email = null, locale = null), headers),
            UserProfileResponse::class.java,
        )

        assertThat(patchResponse.statusCode).isEqualTo(HttpStatus.OK)
        val body = patchResponse.body!!
        assertThat(body.displayName).isEqualTo("Robert")
        assertThat(body.email).isEqualTo("bob@test.com")
        assertThat(body.locale).isEqualTo("de")
    }
}
