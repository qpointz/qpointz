package io.qpointz.mill.security.domain

/**
 * Contract for resolving and provisioning user identities across all authentication methods.
 *
 * Every authentication method (basic/local, OAuth, PAT) must resolve to the same canonical
 * `users.id` through this service. Implementations must never return JPA entity types —
 * only [ResolvedUser] domain objects.
 *
 * Implementations must not declare `@Transactional` on this interface; that annotation
 * belongs on the implementing class only.
 */
interface UserIdentityResolutionService {

    /**
     * Looks up an existing user by provider/subject pair.
     *
     * @param provider authentication provider identifier (e.g. `"local"`, `"entra"`)
     * @param subject provider-specific user identifier (username, OAuth sub, etc.)
     * @return the [ResolvedUser] if a matching identity exists, or `null` otherwise
     */
    fun resolve(provider: String, subject: String): ResolvedUser?

    /**
     * Returns an existing user or provisions a new one on first login.
     *
     * On first call for a given `(provider, subject)` pair, creates a new [UserRecord],
     * [UserIdentityRecord], and empty [UserProfileRecord]. Subsequent calls with the same
     * pair return the same [ResolvedUser.userId] without modification.
     *
     * @param provider authentication provider identifier
     * @param subject provider-specific user identifier
     * @param displayName optional display name hint — used only when creating a new record
     * @param email optional email hint — used only when creating a new record
     * @return the [ResolvedUser] for the resolved or newly provisioned user
     */
    fun resolveOrProvision(
        provider: String,
        subject: String,
        displayName: String? = null,
        email: String? = null,
    ): ResolvedUser

    /**
     * Loads the group names for a user, used to populate [GrantedAuthority] in Spring Security.
     *
     * @param userId canonical `users.id`
     * @return list of group name strings the user belongs to
     */
    fun loadAuthorities(userId: String): List<String>
}
