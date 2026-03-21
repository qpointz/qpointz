package io.qpointz.mill.security.auth.controllers

import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.ErrorResponse
import io.qpointz.mill.security.auth.dto.LoginRequest
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for public (unauthenticated) auth endpoints.
 *
 * Handles `POST /auth/public/login`. This controller performs programmatic authentication
 * using [AuthenticationManager.authenticate] — there is no custom filter. On success,
 * a session is created and `JSESSIONID` is set in the response cookie. On failure, a
 * structured `401` [ErrorResponse] is returned without a redirect.
 *
 * When security is disabled ([securityEnabled] is `false`), login always succeeds and
 * returns an anonymous [AuthMeResponse] without touching the [AuthenticationManager].
 *
 * @property authenticationManager optional — may be absent if no auth provider beans are configured
 * @property identityResolutionService optional — may be absent if only the API module is on classpath
 * @property securityEnabled whether `mill.security.enable` is `true`; when `false` all requests
 *   return an anonymous response
 */
@RestController
@RequestMapping("/auth/public")
class AuthPublicController(
    private val authenticationManager: AuthenticationManager?,
    private val identityResolutionService: UserIdentityResolutionService?,
    private val securityEnabled: Boolean,
) {

    /**
     * Authenticates a user and creates an HTTP session.
     *
     * When security is enabled ([securityEnabled] is `true`):
     * - Calls [AuthenticationManager.authenticate] with the supplied credentials.
     * - On success: creates an [jakarta.servlet.http.HttpSession], stores the
     *   [org.springframework.security.core.context.SecurityContext], and returns
     *   `200 AuthMeResponse` with a `Set-Cookie: JSESSIONID` header.
     * - On failure: returns `401 ErrorResponse` (no redirect).
     *
     * When security is disabled:
     * - Returns `200 AuthMeResponse(userId="anonymous", securityEnabled=false)` immediately.
     *
     * @param request the incoming HTTP request (used to create the session)
     * @param loginRequest JSON body containing `username` and `password`
     * @return `200 AuthMeResponse` on success, `401 ErrorResponse` on failure
     */
    @PostMapping("/login")
    fun login(
        request: HttpServletRequest,
        @RequestBody loginRequest: LoginRequest,
    ): ResponseEntity<Any> {
        if (!securityEnabled) {
            return ResponseEntity.ok(anonymousResponse())
        }

        val manager = authenticationManager
            ?: return ResponseEntity.ok(anonymousResponse())

        return try {
            val token = UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            val authentication = manager.authenticate(token)

            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = authentication

            val session = request.getSession(true)
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
            SecurityContextHolder.setContext(context)

            val response = buildAuthMeResponse(authentication.name, authentication.authorities.map { it.authority })
            ResponseEntity.ok(response)
        } catch (ex: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse(401, "Unauthorized", "Invalid username or password")
            )
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse(401, "Unauthorized", "Authentication failed")
            )
        }
    }

    private fun buildAuthMeResponse(username: String, groups: List<String>): AuthMeResponse {
        val svc = identityResolutionService
        val resolved = svc?.resolve("local", username)
        return AuthMeResponse(
            userId = resolved?.userId ?: username,
            email = resolved?.primaryEmail,
            displayName = resolved?.displayName,
            groups = groups,
            securityEnabled = true,
        )
    }

    private fun anonymousResponse() = AuthMeResponse(
        userId = "anonymous",
        email = null,
        displayName = null,
        groups = emptyList(),
        securityEnabled = false,
    )
}
