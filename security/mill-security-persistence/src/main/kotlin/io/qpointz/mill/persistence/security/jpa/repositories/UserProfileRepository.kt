package io.qpointz.mill.persistence.security.jpa.repositories

import io.qpointz.mill.persistence.security.jpa.entities.UserProfileRecord
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for [UserProfileRecord] entities.
 */
interface UserProfileRepository : JpaRepository<UserProfileRecord, String> {

    /**
     * Finds the profile record for the given user.
     *
     * @param userId canonical [UserRecord.userId]
     * @return the [UserProfileRecord] if a profile exists, or `null` if not yet created
     */
    fun findByUserId(userId: String): UserProfileRecord?
}
