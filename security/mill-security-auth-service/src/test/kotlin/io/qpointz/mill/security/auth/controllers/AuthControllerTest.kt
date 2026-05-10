package io.qpointz.mill.security.auth.controllers

import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.domain.ResolvedUser
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import io.qpointz.mill.security.domain.UserStatus
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

@ExtendWith(MockitoExtension::class)
class AuthControllerTest {

    private val identityService: UserIdentityResolutionService = mock()
    private val request: HttpServletRequest = mock()
    private val session: HttpSession = mock()

    @Test
    fun `getMe_whenSecurityOff_returns200AnonymousResponse`() {
        val controller = AuthController(identityService, securityEnabled = false)

        val response = controller.getMe(null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body as AuthMeResponse
        assertThat(body.userId).isEqualTo("anonymous")
        assertThat(body.securityEnabled).isFalse()
    }

    @Test
    fun `getMe_whenNullAuthentication_returns401`() {
        val controller = AuthController(identityService, securityEnabled = true)

        val response = controller.getMe(null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `getMe_whenAnonymousUserPrincipal_returns401`() {
        val controller = AuthController(identityService, securityEnabled = true)
        val authentication = UsernamePasswordAuthenticationToken("anonymousUser", null, emptyList())

        val response = controller.getMe(authentication)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `getMe_whenAuthenticatedLocalUser_resolverReturnsUser_returns200WithCorrectUserId`() {
        val controller = AuthController(identityService, securityEnabled = true)
        val authentication = UsernamePasswordAuthenticationToken(
            "alice", null, listOf(SimpleGrantedAuthority("testers"))
        )
        whenever(identityService.resolve("local", "alice")).thenReturn(
            ResolvedUser("user-uuid-1", "Alice", "alice@example.com", UserStatus.ACTIVE)
        )

        val response = controller.getMe(authentication)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body as AuthMeResponse
        assertThat(body.userId).isEqualTo("user-uuid-1")
        assertThat(body.email).isEqualTo("alice@example.com")
        assertThat(body.displayName).isEqualTo("Alice")
        assertThat(body.groups).containsExactly("testers")
        assertThat(body.securityEnabled).isTrue()
    }

    @Test
    fun `getMe_whenResolverReturnsNull_returns401`() {
        val controller = AuthController(identityService, securityEnabled = true)
        val authentication = UsernamePasswordAuthenticationToken(
            "unknownuser", null, listOf(SimpleGrantedAuthority("users"))
        )
        whenever(identityService.resolve("local", "unknownuser")).thenReturn(null)

        val response = controller.getMe(authentication)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `getMe_whenOAuthUserNotYetProvisioned_resolveOrProvisionsAndReturns200`() {
        val controller = AuthController(identityService, securityEnabled = true)
        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("OIDC_USER")),
            mapOf(
                "sub" to "authentik-sub-1",
                "email" to "oauth@example.com",
                "name" to "OAuth User",
            ),
            "sub",
        )
        val authentication = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "authentik")
        whenever(identityService.resolve("authentik", "authentik-sub-1")).thenReturn(null)
        whenever(
            identityService.resolveOrProvision("authentik", "authentik-sub-1", "OAuth User", "oauth@example.com"),
        ).thenReturn(ResolvedUser("new-user-id", "OAuth User", "oauth@example.com", UserStatus.ACTIVE))

        val response = controller.getMe(authentication)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body as AuthMeResponse
        assertThat(body.userId).isEqualTo("new-user-id")
        assertThat(body.email).isEqualTo("oauth@example.com")
        assertThat(body.displayName).isEqualTo("OAuth User")
    }

    @Test
    fun `logout_whenSessionExists_invalidatesSessionAndReturns200`() {
        val controller = AuthController(identityService, securityEnabled = true)
        whenever(request.getSession(false)).thenReturn(session)

        val response = controller.logout(request, null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        org.mockito.kotlin.verify(session).invalidate()
    }

    @Test
    fun `logout_whenNoSession_returns200WithoutException`() {
        val controller = AuthController(identityService, securityEnabled = true)
        whenever(request.getSession(false)).thenReturn(null)

        val response = controller.logout(request, null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}
