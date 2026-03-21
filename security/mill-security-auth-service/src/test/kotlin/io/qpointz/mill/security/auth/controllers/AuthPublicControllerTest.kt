package io.qpointz.mill.security.auth.controllers

import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.ErrorResponse
import io.qpointz.mill.security.auth.dto.LoginRequest
import io.qpointz.mill.security.domain.ResolvedUser
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import io.qpointz.mill.security.domain.UserStatus
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

@ExtendWith(MockitoExtension::class)
class AuthPublicControllerTest {

    private val authManager: AuthenticationManager = mock()
    private val identityService: UserIdentityResolutionService = mock()
    private val request: HttpServletRequest = mock()
    private val session: HttpSession = mock()

    @Test
    fun `login_whenSecurityOnAndCorrectCredentials_returns200WithAuthMeResponse`() {
        val controller = AuthPublicController(authManager, identityService, securityEnabled = true)
        val loginRequest = LoginRequest("alice", "secret")

        val authToken = UsernamePasswordAuthenticationToken(
            "alice", null, listOf(SimpleGrantedAuthority("testers"))
        )
        whenever(authManager.authenticate(any())).thenReturn(authToken)
        whenever(request.getSession(true)).thenReturn(session)
        whenever(identityService.resolve("local", "alice")).thenReturn(
            ResolvedUser("user-uuid-1", "Alice", "alice@example.com", UserStatus.ACTIVE)
        )

        val response = controller.login(request, loginRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body as AuthMeResponse
        assertThat(body.userId).isEqualTo("user-uuid-1")
        assertThat(body.email).isEqualTo("alice@example.com")
        assertThat(body.displayName).isEqualTo("Alice")
        assertThat(body.groups).containsExactly("testers")
        assertThat(body.securityEnabled).isTrue()
    }

    @Test
    fun `login_whenSecurityOnAndBadCredentials_returns401ErrorResponse`() {
        val controller = AuthPublicController(authManager, identityService, securityEnabled = true)
        val loginRequest = LoginRequest("alice", "wrongpassword")

        whenever(authManager.authenticate(any())).thenThrow(BadCredentialsException("Bad credentials"))

        val response = controller.login(request, loginRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        val body = response.body as ErrorResponse
        assertThat(body.status).isEqualTo(401)
        assertThat(body.error).isEqualTo("Unauthorized")
        assertThat(body.message).isEqualTo("Invalid username or password")
    }

    @Test
    fun `login_whenSecurityOff_returns200AnonymousResponse`() {
        val controller = AuthPublicController(null, null, securityEnabled = false)
        val loginRequest = LoginRequest("alice", "anypassword")

        val response = controller.login(request, loginRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body as AuthMeResponse
        assertThat(body.userId).isEqualTo("anonymous")
        assertThat(body.securityEnabled).isFalse()
        assertThat(body.groups).isEmpty()
    }

    @Test
    fun `login_whenSecurityOnAndGenericException_returns401ErrorResponse`() {
        val controller = AuthPublicController(authManager, identityService, securityEnabled = true)
        val loginRequest = LoginRequest("alice", "secret")

        whenever(authManager.authenticate(any())).thenThrow(RuntimeException("Unexpected error"))

        val response = controller.login(request, loginRequest)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        val body = response.body as ErrorResponse
        assertThat(body.status).isEqualTo(401)
        assertThat(body.error).isEqualTo("Unauthorized")
        assertThat(body.message).isEqualTo("Authentication failed")
    }
}
