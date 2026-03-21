package io.qpointz.mill.persistence.security.jpa.repositories

import io.qpointz.mill.persistence.security.jpa.entities.UserCredentialRecord
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for [UserCredentialRecord] entities.
 */
interface UserCredentialRepository : JpaRepository<UserCredentialRecord, String> {

    /**
     * Finds an enabled credential for the given user.
     *
     * Used during authentication to load the stored password hash.
     *
     * @param userId canonical [UserRecord.userId]
     * @return the enabled [UserCredentialRecord] for this user, or `null` if none exists
     */
    fun findByUserIdAndEnabledTrue(userId: String): UserCredentialRecord?
}
