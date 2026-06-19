package io.qpointz.mill.ai.autoconfigure.chat

import io.qpointz.mill.ai.chat.UserIdResolver
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

/**
 * Resolves the current chat user from Spring Security when enabled, otherwise delegates to [fallback].
 */
class SecurityUserIdResolver(
    private val identityResolutionService: UserIdentityResolutionService,
    private val securityEnabled: Boolean,
    private val fallback: UserIdResolver,
) : UserIdResolver {

    override fun resolve(): String {
        if (!securityEnabled) {
            return fallback.resolve()
        }
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return fallback.resolve()
        if (!authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            return fallback.resolve()
        }
        val (provider, subject) = extractProviderSubject(authentication) ?: return fallback.resolve()
        return identityResolutionService.resolve(provider, subject)?.userId ?: fallback.resolve()
    }

    private fun extractProviderSubject(authentication: Authentication): Pair<String, String>? =
        when (authentication) {
            is OAuth2AuthenticationToken ->
                Pair(
                    authentication.authorizedClientRegistrationId,
                    authentication.principal?.name ?: return null,
                )
            else -> Pair("local", authentication.name)
        }
}
