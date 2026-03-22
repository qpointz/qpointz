package io.qpointz.mill.security.auth.controllers

import io.qpointz.mill.persistence.security.jpa.entities.UserProfileRecord
import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.UserProfilePatch
import io.qpointz.mill.security.auth.dto.UserProfileResponse
import io.qpointz.mill.security.auth.service.UserProfileService
import io.qpointz.mill.security.domain.ResolvedUser
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import io.qpointz.mill.security.domain.UserStatus
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AuthControllerProfileTest {

    private val identityService: UserIdentityResolutionService = mock()
    private val userProfileService: UserProfileService = mock()
    private val request: HttpServletRequest = mock()

    private fun resolvedAlice() = ResolvedUser("user-uuid-1", "Alice", "alice@example.com", UserStatus.ACTIVE)

    private fun profileRecord(userId: String) = UserProfileRecord(
        userId = userId,
        displayName = "Alice Profile",
        email = "alice@profile.com",
        theme = null,
        locale = "en",
        updatedAt = Instant.now(),
    )

    private fun aliceAuth() = UsernamePasswordAuthenticationToken(
        "alice", null, listOf(SimpleGrantedAuthority("testers"))
    )

    @Test
    fun `getMe_returnsProfileWithCorrectFields`() {
        val controller = AuthController(identityService, securityEnabled = true, userProfileService = userProfileService)
        whenever(identityService.resolve("local", "alice")).thenReturn(resolvedAlice())
        whenever(userProfileService.getOrCreate("user-uuid-1")).thenReturn(profileRecord("user-uuid-1"))

        val response = controller.getMe(aliceAuth())

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body as AuthMeResponse
        assertThat(body.profile).isNotNull
        assertThat(body.profile!!.userId).isEqualTo("user-uuid-1")
        assertThat(body.profile!!.displayName).isEqualTo("Alice Profile")
        assertThat(body.profile!!.email).isEqualTo("alice@profile.com")
        assertThat(body.profile!!.locale).isEqualTo("en")
    }

    @Test
    fun `getMe_whenProfileServiceAbsent_returnsNullProfile`() {
        val controller = AuthController(identityService, securityEnabled = true, userProfileService = null)
        whenever(identityService.resolve("local", "alice")).thenReturn(resolvedAlice())

        val response = controller.getMe(aliceAuth())

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body as AuthMeResponse
        assertThat(body.profile).isNull()
    }

    @Test
    fun `updateProfile_returnsUpdatedProfile`() {
        val controller = AuthController(identityService, securityEnabled = true, userProfileService = userProfileService)
        whenever(identityService.resolve("local", "alice")).thenReturn(resolvedAlice())

        val updatedRecord = UserProfileRecord(
            userId = "user-uuid-1",
            displayName = "Updated Alice",
            email = "updated@example.com",
            theme = null,
            locale = "fr",
            updatedAt = Instant.now(),
        )
        whenever(userProfileService.update(any<String>(), any<UserProfilePatch>())).thenReturn(updatedRecord)

        val patch = UserProfilePatch(displayName = "Updated Alice", email = "updated@example.com", locale = "fr")
        val response = controller.updateProfile(request, aliceAuth(), patch)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body as UserProfileResponse
        assertThat(body.userId).isEqualTo("user-uuid-1")
        assertThat(body.displayName).isEqualTo("Updated Alice")
        assertThat(body.email).isEqualTo("updated@example.com")
        assertThat(body.locale).isEqualTo("fr")
    }

    @Test
    fun `updateProfile_whenUnauthenticated_returns401`() {
        val controller = AuthController(identityService, securityEnabled = true, userProfileService = userProfileService)

        val response = controller.updateProfile(request, null, UserProfilePatch(null, null, null))

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `updateProfile_whenSecurityOff_returns401`() {
        val controller = AuthController(identityService, securityEnabled = false, userProfileService = userProfileService)

        val response = controller.updateProfile(request, aliceAuth(), UserProfilePatch(displayName = "X", null, null))

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
