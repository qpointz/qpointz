package io.qpointz.mill.persistence.security.jpa.repositories

import io.qpointz.mill.persistence.security.jpa.entities.UserRecord
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for canonical [UserRecord] entities.
 */
interface UserRepository : JpaRepository<UserRecord, String> {

    /**
     * Finds a user by their primary email address.
     *
     * @param primaryEmail the email address to search for
     * @return the matching [UserRecord], or `null` if not found
     */
    fun findByPrimaryEmail(primaryEmail: String): UserRecord?
}
