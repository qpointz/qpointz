package io.qpointz.mill.security.auth.filestore

import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.LoginRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

/**
 * Integration test for secure-minimal style file-backed basic auth via autoconfiguration
 * ({@code store: classpath:config/auth.yml}). Uses a dedicated application package so
 * [io.qpointz.mill.security.auth.TestAuthServiceApplication] is not component-scanned.
 */
@SpringBootTest(
    classes = [FileStoreAuthTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(
    properties = [
        "mill.security.enable=true",
        "mill.security.allow-registration=false",
        "mill.security.authentication.basic.enable=true",
        "mill.security.authentication.basic.store=classpath:config/auth.yml",
    ],
)
class FileStoreAuthIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    private fun client(): WebTestClient =
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .responseTimeout(Duration.ofSeconds(10))
            .build()

    @Test
    fun `POST auth-public-login with admin credentials returns 200`() {
        val result = client()
            .post()
            .uri("/auth/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(LoginRequest("admin", "admin"))
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthMeResponse::class.java)
            .returnResult()

        val body = result.responseBody!!
        assertThat(body.securityEnabled).isTrue()
        assertThat(body.userId).isNotBlank().isNotEqualTo("anonymous")
        assertThat(result.responseCookies["JSESSIONID"]?.firstOrNull()?.value).isNotBlank()
    }
}
