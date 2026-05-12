package io.qpointz.mill.data.query.web

import io.qpointz.mill.data.query.engine.CallerContext
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.server.ResponseStatusException

/**
 * Maps Spring [Authentication] to [CallerContext] for query-result session ownership.
 *
 * @param authentication current security context authentication, if any
 * @return caller context with stable tenant key
 * @throws ResponseStatusException HTTP 401 when the caller is not a recognized authenticated principal
 */
fun requireCallerContext(authentication: Authentication?): CallerContext {
    if (authentication == null || !authentication.isAuthenticated) {
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated")
    }
    if (authentication is AnonymousAuthenticationToken) {
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated")
    }
    val tenant = when (val p = authentication.principal) {
        is UserDetails -> p.username
        else -> authentication.name
    }
    if (tenant.isBlank()) {
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated")
    }
    return CallerContext(tenant = tenant)
}
