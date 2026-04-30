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
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Integration tests for the user profile endpoints.
 *
 * Spring Boot 4 removed `TestRestTemplate`; these ITs use [WebTestClient] against a real server.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfileIntegrationTest {

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
                ),
            )
        }
    }

    private fun loginAndGetSession(): String {
        val loginRequest = LoginRequest("profileuser", "testpass")
        val result = client()
            .post()
            .uri("/auth/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthMeResponse::class.java)
            .returnResult()

        val session = result.responseCookies["JSESSIONID"]?.firstOrNull()?.value
        assertThat(session).isNotBlank()
        return session!!
    }

    @Test
    fun `GET auth-me after login returns non-null profile auto-created`() {
        val session = loginAndGetSession()

        val body = client()
            .get()
            .uri("/auth/me")
            .cookie("JSESSIONID", session)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthMeResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(body.profile).isNotNull
        assertThat(body.profile!!.userId).isNotBlank()
    }

    @Test
    fun `PATCH auth-profile persists displayName reflected in subsequent GET auth-me`() {
        val session = loginAndGetSession()

        val patch = UserProfilePatch(displayName = "Alice", email = null, locale = null)
        val patchBody = client()
            .patch()
            .uri("/auth/profile")
            .cookie("JSESSIONID", session)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(patch)
            .exchange()
            .expectStatus().isOk
            .expectBody(UserProfileResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(patchBody.displayName).isEqualTo("Alice")

        val meBody = client()
            .get()
            .uri("/auth/me")
            .cookie("JSESSIONID", session)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthMeResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(meBody.profile!!.displayName).isEqualTo("Alice")
    }

    @Test
    fun `PATCH auth-profile partial update leaves other fields unchanged`() {
        val session = loginAndGetSession()

        client()
            .patch()
            .uri("/auth/profile")
            .cookie("JSESSIONID", session)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UserProfilePatch(displayName = "Bob", email = "bob@test.com", locale = "de"))
            .exchange()
            .expectStatus().isOk

        val patchBody = client()
            .patch()
            .uri("/auth/profile")
            .cookie("JSESSIONID", session)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UserProfilePatch(displayName = "Robert", email = null, locale = null))
            .exchange()
            .expectStatus().isOk
            .expectBody(UserProfileResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(patchBody.displayName).isEqualTo("Robert")
        assertThat(patchBody.email).isEqualTo("bob@test.com")
        assertThat(patchBody.locale).isEqualTo("de")
    }
}
