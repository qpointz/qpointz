package io.qpointz.mill.ai.chat

/**
 * Extension point for resolving the current user identity.
 *
 * Spring Boot hosts register a default bean from `mill-ai.chat.default-user-id`.
 * Override the bean to integrate with Spring Security or any other authentication mechanism.
 */
fun interface UserIdResolver {
    fun resolve(): String
}

/**
 * Fallback resolver that returns a fixed user id.
 * Suitable for single-user deployments or development; replace with a
 * security-aware implementation for multi-user production use.
 *
 * @param userId static value returned from [resolve]
 */
class PropertiesUserIdResolver(private val userId: String) : UserIdResolver {
    override fun resolve(): String = userId
}
