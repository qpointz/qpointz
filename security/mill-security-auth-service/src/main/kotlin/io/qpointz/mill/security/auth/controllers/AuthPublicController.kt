package io.qpointz.mill.security.auth.controllers

import io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.security.audit.AuthAuditService
import io.qpointz.mill.security.audit.AuthEventType
import io.qpointz.mill.security.auth.dto.AuthMeResponse
import io.qpointz.mill.security.auth.dto.ErrorResponse
import io.qpointz.mill.security.auth.dto.LoginRequest
import io.qpointz.mill.security.auth.dto.RegisterRequest
import io.qpointz.mill.security.domain.PasswordHasher
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
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
import java.time.Instant
import java.util.UUID

/**
 * REST controller for public (unauthenticated) auth endpoints.
 *
 * Handles `POST /auth/public/login` and `POST /auth/public/register`. The login endpoint
 * performs programmatic authentication using [AuthenticationManager.authenticate] — there
 * is no custom filter. On success, a session is created and `JSESSIONID` is set in the
 * response cookie. On failure, a structured [ErrorResponse] is returned without a redirect.
 *
 * The register endpoint creates a new local (password-based) user when
 * `mill.security.allow-registration` is `true`. It delegates provisioning to
 * [UserIdentityResolutionService.resolveOrProvision] and persists the hashed credential
 * via [UserCredentialRepository].
 *
 * When security is disabled ([securityEnabled] is `false`), login always succeeds and
 * returns an anonymous [AuthMeResponse] without touching the [AuthenticationManager].
 * Registration always returns `403` when security is disabled.
 *
 * All security-relevant operations are written to the application log and, when
 * [authAuditService] is present, to the persistent `auth_events` table.
 *
 * @property authenticationManager optional — may be absent if no auth provider beans are configured
 * @property identityResolutionService optional — may be absent if only the API module is on classpath
 * @property securityEnabled whether `mill.security.enable` is `true`; when `false` all requests
 *   return an anonymous response
 * @property allowRegistration whether `mill.security.allow-registration` is `true`; when `false`
 *   the register endpoint returns `403`
 * @property userIdentityRepository optional — required for the register endpoint; absent without
 *   the `mill-security-persistence` module
 * @property userCredentialRepository optional — required for the register endpoint; absent without
 *   the `mill-security-persistence` module
 * @property passwordHasher optional — required for the register endpoint; absent without
 *   the `mill-security-persistence` module
 * @property authAuditService optional — when present each auth event is persisted to `auth_events`
 */
@RestController
@RequestMapping("/auth/public")
class AuthPublicController(
    private val authenticationManager: AuthenticationManager?,
    private val identityResolutionService: UserIdentityResolutionService?,
    private val securityEnabled: Boolean,
    private val allowRegistration: Boolean = false,
    private val userIdentityRepository: UserIdentityRepository? = null,
    private val userCredentialRepository: UserCredentialRepository? = null,
    private val passwordHasher: PasswordHasher? = null,
    private val authAuditService: AuthAuditService? = null,
) {

    private val log = LoggerFactory.getLogger(AuthPublicController::class.java)

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
     * @param request the incoming HTTP request (used to create the session and extract IP/UA)
     * @param loginRequest JSON body containing `username` and `password`
     * @return `200 AuthMeResponse` on success, `401 ErrorResponse` on failure
     */
    @PostMapping("/login")
    fun login(
        request: HttpServletRequest,
        @RequestBody loginRequest: LoginRequest,
    ): ResponseEntity<Any> {
        if (!securityEnabled) {
            log.debug("Login skipped: security disabled, returning anonymous response")
            return ResponseEntity.ok(anonymousResponse())
        }

        val manager = authenticationManager
            ?: return ResponseEntity.ok(anonymousResponse())

        log.info("Login attempt: subject={} ip={}", loginRequest.username, ipAddress(request))

        return try {
            val token = UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            val authentication = manager.authenticate(token)

            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = authentication

            val session = request.getSession(true)
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
            SecurityContextHolder.setContext(context)

            val response = buildAuthMeResponse(
                authentication.name,
                authentication.authorities.mapNotNull { it.authority }
            )
            log.info("Login success: subject={} userId={} ip={}", loginRequest.username, response.userId, ipAddress(request))
            authAuditService?.record(
                type = AuthEventType.LOGIN_SUCCESS,
                subject = loginRequest.username,
                userId = response.userId,
                ipAddress = ipAddress(request),
                userAgent = userAgent(request),
            )
            ResponseEntity.ok(response)
        } catch (ex: BadCredentialsException) {
            log.warn("Login failure: subject={} reason=BAD_CREDENTIALS ip={}", loginRequest.username, ipAddress(request))
            authAuditService?.record(
                type = AuthEventType.LOGIN_FAILURE,
                subject = loginRequest.username,
                userId = null,
                ipAddress = ipAddress(request),
                userAgent = userAgent(request),
                failureReason = "BAD_CREDENTIALS",
            )
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse(401, "Unauthorized", "Invalid username or password")
            )
        } catch (ex: Exception) {
            log.warn("Login failure: subject={} reason=AUTH_ERROR ip={} error={}", loginRequest.username, ipAddress(request), ex.message)
            authAuditService?.record(
                type = AuthEventType.LOGIN_FAILURE,
                subject = loginRequest.username,
                userId = null,
                ipAddress = ipAddress(request),
                userAgent = userAgent(request),
                failureReason = "AUTH_ERROR",
            )
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse(401, "Unauthorized", "Authentication failed")
            )
        }
    }

    /**
     * Registers a new local user account and opens an authenticated session.
     *
     * Prerequisites checked in order:
     * 1. `mill.security.allow-registration` must be `true` — returns `403` otherwise.
     * 2. [email][RegisterRequest.email] must match basic email format — returns `422` otherwise.
     * 3. [password][RegisterRequest.password] must be non-blank — returns `422` otherwise.
     * 4. No existing identity for `("local", email)` — returns `409` if already registered.
     *
     * On success:
     * - Delegates to [UserIdentityResolutionService.resolveOrProvision] to create the
     *   canonical user record and identity mapping.
     * - Persists a [UserCredentialRecord] with the hashed password.
     * - Creates an [jakarta.servlet.http.HttpSession] and stores the
     *   [org.springframework.security.core.context.SecurityContext] (same flow as login).
     * - Returns `201 AuthMeResponse`.
     *
     * @param request the incoming HTTP request (used to create the session and extract IP/UA)
     * @param registerRequest JSON body containing `email`, `password`, and optional `displayName`
     * @return `201 AuthMeResponse` on success; `403`, `409`, or `422 ErrorResponse` on failure
     */
    @PostMapping("/register")
    fun register(
        request: HttpServletRequest,
        @RequestBody registerRequest: RegisterRequest,
    ): ResponseEntity<Any> {
        if (!allowRegistration) {
            log.warn("Registration rejected: registration disabled ip={}", ipAddress(request))
            authAuditService?.record(
                type = AuthEventType.REGISTER_FAILURE,
                subject = registerRequest.email,
                userId = null,
                ipAddress = ipAddress(request),
                userAgent = userAgent(request),
                failureReason = "REGISTRATION_DISABLED",
            )
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse(403, "Forbidden", "Registration is disabled")
            )
        }

        val email = registerRequest.email.trim()
        if (!isValidEmail(email)) {
            log.warn("Registration rejected: invalid email subject={} ip={}", email, ipAddress(request))
            authAuditService?.record(
                type = AuthEventType.REGISTER_FAILURE,
                subject = email,
                userId = null,
                ipAddress = ipAddress(request),
                userAgent = userAgent(request),
                failureReason = "VALIDATION_ERROR",
            )
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ErrorResponse(422, "Unprocessable Entity", "Invalid email address")
            )
        }

        if (registerRequest.password.isBlank()) {
            log.warn("Registration rejected: blank password subject={} ip={}", email, ipAddress(request))
            authAuditService?.record(
                type = AuthEventType.REGISTER_FAILURE,
                subject = email,
                userId = null,
                ipAddress = ipAddress(request),
                userAgent = userAgent(request),
                failureReason = "VALIDATION_ERROR",
            )
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ErrorResponse(422, "Unprocessable Entity", "Password must not be blank")
            )
        }

        val identityRepo = userIdentityRepository
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse(500, "Internal Server Error", "Registration infrastructure unavailable")
            )

        val credentialRepo = userCredentialRepository
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse(500, "Internal Server Error", "Registration infrastructure unavailable")
            )

        val hasher = passwordHasher
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse(500, "Internal Server Error", "Registration infrastructure unavailable")
            )

        log.info("Registration attempt: subject={} ip={}", email, ipAddress(request))

        val existing = identityRepo.findByProviderAndSubject("local", email)
        if (existing != null) {
            log.warn("Registration conflict: subject={} ip={}", email, ipAddress(request))
            authAuditService?.record(
                type = AuthEventType.REGISTER_FAILURE,
                subject = email,
                userId = null,
                ipAddress = ipAddress(request),
                userAgent = userAgent(request),
                failureReason = "DUPLICATE_EMAIL",
            )
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse(409, "Conflict", "An account with this email already exists")
            )
        }

        val resolutionService = identityResolutionService
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse(500, "Internal Server Error", "Registration infrastructure unavailable")
            )

        val displayName = registerRequest.displayName?.takeIf { it.isNotBlank() } ?: email
        val resolved = resolutionService.resolveOrProvision("local", email, displayName, email)

        val now = Instant.now()
        credentialRepo.save(
            UserCredentialRecord(
                credentialId = UUID.randomUUID().toString(),
                userId = resolved.userId,
                passwordHash = hasher.hash(registerRequest.password),
                algorithm = hasher.algorithmId,
                enabled = true,
                createdAt = now,
                updatedAt = now,
            )
        )

        val manager = authenticationManager
        if (manager != null) {
            try {
                val token = UsernamePasswordAuthenticationToken(email, registerRequest.password)
                val authentication = manager.authenticate(token)
                val context = SecurityContextHolder.createEmptyContext()
                context.authentication = authentication
                val session = request.getSession(true)
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
                SecurityContextHolder.setContext(context)
            } catch (_: Exception) {
                // Session setup is best-effort; the account was created successfully
            }
        }

        log.info("Registration success: subject={} userId={} ip={}", email, resolved.userId, ipAddress(request))
        authAuditService?.record(
            type = AuthEventType.REGISTER_SUCCESS,
            subject = email,
            userId = resolved.userId,
            ipAddress = ipAddress(request),
            userAgent = userAgent(request),
        )

        val response = AuthMeResponse(
            userId = resolved.userId,
            email = resolved.primaryEmail,
            displayName = resolved.displayName,
            groups = emptyList(),
            securityEnabled = true,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
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

    /**
     * Extracts the originating IP address from the request.
     *
     * Checks the `X-Forwarded-For` header first (set by reverse proxies); falls back to
     * [HttpServletRequest.remoteAddr] when the header is absent.
     *
     * @param request the incoming HTTP request
     * @return the client IP address string, or `null` if unavailable
     */
    private fun ipAddress(request: HttpServletRequest): String? =
        request.getHeader("X-Forwarded-For")?.substringBefore(',')?.trim()
            ?: request.remoteAddr

    /**
     * Extracts the `User-Agent` header value from the request.
     *
     * @param request the incoming HTTP request
     * @return the User-Agent string, or `null` if the header is absent
     */
    private fun userAgent(request: HttpServletRequest): String? =
        request.getHeader("User-Agent")

    /**
     * Simple email format check using a standard pattern.
     *
     * Validates that [email] contains a local part, an `@` symbol, and a domain with at
     * least one dot. This is intentionally permissive — it rejects clearly malformed
     * inputs without over-constraining valid exotic addresses.
     *
     * @param email the candidate email string to validate
     * @return `true` if [email] appears to be a valid email address
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        return emailRegex.matches(email)
    }
}
