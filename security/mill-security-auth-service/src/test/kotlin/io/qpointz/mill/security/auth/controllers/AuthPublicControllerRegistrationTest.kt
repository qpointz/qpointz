package io.qpointz.mill.security.auth.controllers

import io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.ErrorResponse
import io.qpointz.mill.security.auth.dto.RegisterRequest
import io.qpointz.mill.security.domain.PasswordHasher
import io.qpointz.mill.security.domain.ResolvedUser
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import io.qpointz.mill.security.domain.UserStatus
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

@ExtendWith(MockitoExtension::class)
class AuthPublicControllerRegistrationTest {

    private val authManager: AuthenticationManager = mock()
    private val identityService: UserIdentityResolutionService = mock()
    private val identityRepository: UserIdentityRepository = mock()
    private val credentialRepository: UserCredentialRepository = mock()
    private val request: HttpServletRequest = mock()
    private val session: HttpSession = mock()
    private val hasher: PasswordHasher = mock()

    @BeforeEach
    fun setup() {
        whenever(hasher.algorithmId).thenReturn("noop")
        whenever(hasher.hash(any())).thenAnswer { "{noop}${it.arguments[0]}" }
        whenever(request.getSession(true)).thenReturn(session)
    }

    private fun controller(allowRegistration: Boolean = true): AuthPublicController =
        AuthPublicController(
            authenticationManager = authManager,
            identityResolutionService = identityService,
            securityEnabled = true,
            allowRegistration = allowRegistration,
            userIdentityRepository = identityRepository,
            userCredentialRepository = credentialRepository,
            passwordHasher = hasher,
        )

    @Test
    fun `register_whenAllEnabled_returns201WithAuthMeResponse`() {
        val registerRequest = RegisterRequest("alice@example.com", "secret", "Alice")

        whenever(identityRepository.findByProviderAndSubject("local", "alice@example.com")).thenReturn(null)
        whenever(identityService.resolveOrProvision("local", "alice@example.com", "Alice", "alice@example.com"))
            .thenReturn(ResolvedUser("user-uuid-1", "Alice", "alice@example.com", UserStatus.ACTIVE))
        whenever(credentialRepository.save(any<UserCredentialRecord>())).thenAnswer { it.arguments[0] }
        whenever(authManager.authenticate(any())).thenReturn(
            UsernamePasswordAuthenticationToken("alice@example.com", null, listOf(SimpleGrantedAuthority("users")))
        )

        val response = controller().register(request, registerRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val body = response.body as AuthMeResponse
        assertThat(body.userId).isEqualTo("user-uuid-1")
        assertThat(body.email).isEqualTo("alice@example.com")
        assertThat(body.displayName).isEqualTo("Alice")
        assertThat(body.securityEnabled).isTrue()
    }

    @Test
    fun `register_whenRegistrationDisabled_returns403`() {
        val registerRequest = RegisterRequest("alice@example.com", "secret", null)

        val response = controller(allowRegistration = false).register(request, registerRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        val body = response.body as ErrorResponse
        assertThat(body.status).isEqualTo(403)
        assertThat(body.error).isEqualTo("Forbidden")
    }

    @Test
    fun `register_whenEmailAlreadyExists_returns409`() {
        val registerRequest = RegisterRequest("alice@example.com", "secret", null)

        val existingRecord = mock<io.qpointz.mill.persistence.security.jpa.entities.UserIdentityRecord>()
        whenever(identityRepository.findByProviderAndSubject("local", "alice@example.com"))
            .thenReturn(existingRecord)

        val response = controller().register(request, registerRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        val body = response.body as ErrorResponse
        assertThat(body.status).isEqualTo(409)
        assertThat(body.error).isEqualTo("Conflict")
    }

    @Test
    fun `register_whenEmailInvalid_returns422`() {
        val registerRequest = RegisterRequest("not-an-email", "secret", null)

        val response = controller().register(request, registerRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        val body = response.body as ErrorResponse
        assertThat(body.status).isEqualTo(422)
        assertThat(body.error).isEqualTo("Unprocessable Entity")
    }

    @Test
    fun `register_whenPasswordBlank_returns422`() {
        val registerRequest = RegisterRequest("alice@example.com", "   ", null)

        whenever(identityRepository.findByProviderAndSubject("local", "alice@example.com")).thenReturn(null)

        val response = controller().register(request, registerRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        val body = response.body as ErrorResponse
        assertThat(body.status).isEqualTo(422)
        assertThat(body.error).isEqualTo("Unprocessable Entity")
    }

    @Test
    fun `register_whenDisplayNameAbsent_usesEmailAsDisplayName`() {
        val registerRequest = RegisterRequest("alice@example.com", "secret", null)

        whenever(identityRepository.findByProviderAndSubject("local", "alice@example.com")).thenReturn(null)
        whenever(identityService.resolveOrProvision("local", "alice@example.com", "alice@example.com", "alice@example.com"))
            .thenReturn(ResolvedUser("user-uuid-2", "alice@example.com", "alice@example.com", UserStatus.ACTIVE))
        whenever(credentialRepository.save(any<UserCredentialRecord>())).thenAnswer { it.arguments[0] }
        whenever(authManager.authenticate(any())).thenReturn(
            UsernamePasswordAuthenticationToken("alice@example.com", null, emptyList())
        )

        val response = controller().register(request, registerRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }
}
