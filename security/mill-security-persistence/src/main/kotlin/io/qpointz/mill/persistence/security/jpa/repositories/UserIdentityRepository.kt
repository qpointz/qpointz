package io.qpointz.mill.persistence.security.jpa.repositories

import io.qpointz.mill.persistence.security.jpa.entities.UserIdentityRecord
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for [UserIdentityRecord] entities.
 *
 * This is the primary resolution query used by
 * [JpaUserIdentityResolutionService] to map authentication credentials
 * to a canonical user.
 */
interface UserIdentityRepository : JpaRepository<UserIdentityRecord, String> {

    /**
     * Finds the identity record for a given provider/subject pair.
     *
     * This is the primary lookup used during authentication: given a provider and
     * a subject (e.g. username for local auth, OAuth sub for OAuth), it returns
     * the identity record that maps to the canonical [UserRecord.userId].
     *
     * @param provider authentication provider identifier (e.g. `"local"`, `"entra"`)
     * @param subject provider-specific user identifier
     * @return the matching [UserIdentityRecord], or `null` if not found
     */
    fun findByProviderAndSubject(provider: String, subject: String): UserIdentityRecord?

    /**
     * Finds all identity records for a given authentication provider.
     *
     * Used by [io.qpointz.mill.persistence.security.jpa.auth.JpaUserRepo] to enumerate
     * all local users during basic-auth resolution.
     *
     * @param provider authentication provider identifier (e.g. `"local"`)
     * @return all [UserIdentityRecord] entries for that provider
     */
    fun findByProvider(provider: String): List<UserIdentityRecord>
}
