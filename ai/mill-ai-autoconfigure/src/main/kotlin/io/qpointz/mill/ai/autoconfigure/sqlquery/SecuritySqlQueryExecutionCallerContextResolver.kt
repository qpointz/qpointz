package io.qpointz.mill.ai.autoconfigure.sqlquery

import io.qpointz.mill.data.query.engine.CallerContext
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

/**
 * Maps Spring Security authentication to [CallerContext], with a stable fallback tenant when
 * security is disabled or the principal is anonymous.
 *
 * @property fallbackTenant tenant used when no authenticated principal is present
 */
class SecuritySqlQueryExecutionCallerContextResolver(
    private val fallbackTenant: String = "anonymous",
) : SqlQueryExecutionCallerContextResolver {

    override fun resolve(): CallerContext {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return CallerContext(fallbackTenant)
        }
        if (authentication is AnonymousAuthenticationToken) {
            return CallerContext(fallbackTenant)
        }
        val tenant = when (val principal = authentication.principal) {
            is UserDetails -> principal.username
            else -> authentication.name
        }
        return CallerContext(tenant.ifBlank { fallbackTenant })
    }
}
