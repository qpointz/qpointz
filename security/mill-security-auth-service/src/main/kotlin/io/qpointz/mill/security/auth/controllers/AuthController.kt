package io.qpointz.mill.security.auth.controllers

import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.UserProfilePatch
import io.qpointz.mill.security.auth.dto.UserProfileResponse
import io.qpointz.mill.security.auth.service.UserProfileService
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for authenticated auth endpoints.
 *
 * Handles `GET /auth/me` (session check / user info), `PATCH /auth/profile`
 * (partial profile update), and `POST /auth/logout`.
 *
 * @property identityResolutionService optional — may be absent if only the API module is on classpath
 * @property userProfileService optional — absent when security is disabled or persistence is not on classpath
 * @property securityEnabled whether `mill.security.enable` is `true`; when `false` all endpoints
 *   return anonymous responses regardless of which beans are present
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    private val identityResolutionService: UserIdentityResolutionService?,
    private val securityEnabled: Boolean,
    private val userProfileService: UserProfileService? = null,
) {

    /**
     * Returns the currently authenticated user's identity, including their profile.
     *
     * Extracts `(provider, subject)` from the [Authentication] principal and resolves
     * the canonical `userId` via [UserIdentityResolutionService.resolve]:
     * - [org.springframework.security.authentication.UsernamePasswordAuthenticationToken]
     *   → `provider = "local"`, `subject = authentication.name`
     * - [OAuth2AuthenticationToken] → `provider = registrationId`, `subject = principal.name`
     * - Unauthenticated / anonymous → returns `401`
     *
     * When security is disabled ([securityEnabled] is `false`), returns an anonymous
     * [AuthMeResponse] with `securityEnabled = false` without inspecting [authentication].
     *
     * When [userProfileService] is present, the response includes a [UserProfileResponse]
     * with the user's editable profile attributes (lazy-created on first call).
     *
     * @param authentication the current [Authentication]; `null` if not authenticated
     * @return `200 AuthMeResponse` or `401` if unauthenticated
     */
    @GetMapping("/me")
    fun getMe(authentication: Authentication?): ResponseEntity<Any> {
        if (!securityEnabled) {
            return ResponseEntity.ok(anonymousResponse())
        }

        if (authentication == null || !authentication.isAuthenticated ||
            authentication.principal == "anonymousUser") {
            return ResponseEntity.status(401).build()
        }

        val (provider, subject) = extractProviderSubject(authentication)
            ?: return ResponseEntity.status(401).build()

        val resolved = identityResolutionService?.resolve(provider, subject)
            ?: return ResponseEntity.status(401).build()

        val groups = authentication.authorities.map { it.authority }
        val profile = userProfileService?.let { svc ->
            val record = svc.getOrCreate(resolved.userId)
            UserProfileResponse(
                userId = record.userId,
                displayName = record.displayName,
                email = record.email,
                locale = record.locale,
            )
        }

        return ResponseEntity.ok(
            AuthMeResponse(
                userId = resolved.userId,
                email = resolved.primaryEmail,
                displayName = resolved.displayName,
                groups = groups,
                securityEnabled = true,
                profile = profile,
            )
        )
    }

    /**
     * Applies a partial update to the authenticated user's profile.
     *
     * Only non-null fields in [patch] overwrite the stored values; null fields leave
     * the existing stored value intact.
     *
     * @param authentication the current [Authentication]; `null` if not authenticated
     * @param patch JSON body containing optional `displayName`, `email`, and `locale` fields
     * @return `200 UserProfileResponse` with the updated profile, or `401` if unauthenticated
     */
    @PatchMapping("/profile")
    fun updateProfile(
        authentication: Authentication?,
        @RequestBody patch: UserProfilePatch,
    ): ResponseEntity<Any> {
        if (!securityEnabled) {
            return ResponseEntity.status(401).build()
        }

        if (authentication == null || !authentication.isAuthenticated ||
            authentication.principal == "anonymousUser") {
            return ResponseEntity.status(401).build()
        }

        val (provider, subject) = extractProviderSubject(authentication)
            ?: return ResponseEntity.status(401).build()

        val resolved = identityResolutionService?.resolve(provider, subject)
            ?: return ResponseEntity.status(401).build()

        val svc = userProfileService
            ?: return ResponseEntity.status(401).build()

        val record = svc.update(resolved.userId, patch)
        return ResponseEntity.ok(
            UserProfileResponse(
                userId = record.userId,
                displayName = record.displayName,
                email = record.email,
                locale = record.locale,
            )
        )
    }

    /**
     * Invalidates the current HTTP session, logging the user out.
     *
     * Always returns `200` — this is a no-op if the session is already gone.
     *
     * @param request the incoming HTTP request whose session is to be invalidated
     * @return `200` with empty body
     */
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        val session = request.getSession(false)
        session?.invalidate()
        return ResponseEntity.ok().build()
    }

    private fun extractProviderSubject(authentication: Authentication): Pair<String, String>? {
        return when (authentication) {
            is OAuth2AuthenticationToken ->
                Pair(authentication.authorizedClientRegistrationId, authentication.principal.name)
            else ->
                Pair("local", authentication.name)
        }
    }

    private fun anonymousResponse() = AuthMeResponse(
        userId = "anonymous",
        email = null,
        displayName = null,
        groups = emptyList(),
        securityEnabled = false,
        profile = null,
    )
}
