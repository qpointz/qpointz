package io.qpointz.mill.persistence.security.jpa.repositories

import io.qpointz.mill.persistence.security.jpa.entities.AuthEventRecord
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for [AuthEventRecord] entities.
 *
 * Provides standard CRUD operations inherited from [JpaRepository] plus
 * purpose-built finders for common audit queries.
 */
interface AuthEventRepository : JpaRepository<AuthEventRecord, String> {

    /**
     * Returns all audit events associated with a given user, ordered from most recent to oldest.
     *
     * Useful for displaying a chronological activity log on a user's account page or for
     * forensic investigation after an incident.
     *
     * @param userId the canonical user identifier to query; must match [AuthEventRecord.userId]
     * @return list of events for the user, sorted descending by [AuthEventRecord.occurredAt];
     *   empty list if no events exist for the user
     */
    fun findByUserIdOrderByOccurredAtDesc(userId: String): List<AuthEventRecord>
}
