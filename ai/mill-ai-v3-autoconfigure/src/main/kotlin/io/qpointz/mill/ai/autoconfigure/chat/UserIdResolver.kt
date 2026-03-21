package io.qpointz.mill.ai.autoconfigure.chat

/**
 * Extension point for resolving the current user identity.
 *
 * The default bean ([PropertiesUserIdResolver]) returns a static value from
 * `mill.ai.chat.default-user-id`. Override this bean to integrate with Spring Security
 * or any other authentication mechanism:
 *
 * ```kotlin
 * @Bean
 * fun userIdResolver(security: SecurityContext): UserIdResolver =
 *     UserIdResolver { security.authentication?.name ?: "anonymous" }
 * ```
 */
fun interface UserIdResolver {
    fun resolve(): String
}

/**
 * Fallback resolver that returns a fixed user id from configuration.
 * Suitable for single-user deployments or development; replace with a
 * security-aware implementation for multi-user production use.
 */
class PropertiesUserIdResolver(private val userId: String) : UserIdResolver {
    override fun resolve(): String = userId
}
