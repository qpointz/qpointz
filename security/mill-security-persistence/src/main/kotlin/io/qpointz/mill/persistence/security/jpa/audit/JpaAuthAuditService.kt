package io.qpointz.mill.persistence.security.jpa.audit

import io.qpointz.mill.persistence.security.jpa.entities.AuthEventRecord
import io.qpointz.mill.persistence.security.jpa.repositories.AuthEventRepository
import io.qpointz.mill.security.audit.AuthAuditService
import io.qpointz.mill.security.audit.AuthEventType
import java.time.Instant
import java.util.UUID

/**
 * JPA-backed implementation of [AuthAuditService].
 *
 * Persists each event as an [AuthEventRecord] row in the `auth_events` table.
 * A fresh UUID is assigned to [AuthEventRecord.eventId] on every call; the caller
 * does not need to supply one. Writes are performed synchronously — the calling
 * thread blocks until the row is flushed by the JPA provider.
 *
 * @property repository the repository used to persist [AuthEventRecord] entities
 */
class JpaAuthAuditService(
    private val repository: AuthEventRepository,
) : AuthAuditService {

    /**
     * Persists a single authentication or account lifecycle event.
     *
     * @param type the category of the event
     * @param subject username, email, or other identity token presented in the request;
     *   `null` when unavailable
     * @param userId resolved canonical user identifier; `null` when the subject could not
     *   be matched to a known user (e.g. failed login with an unknown username)
     * @param ipAddress originating IP address of the HTTP request; `null` if unavailable
     * @param userAgent value of the `User-Agent` request header; `null` if absent
     * @param failureReason short machine-readable rejection code; `null` for success events
     */
    override fun record(
        type: AuthEventType,
        subject: String?,
        userId: String?,
        ipAddress: String?,
        userAgent: String?,
        failureReason: String?,
    ) {
        repository.save(
            AuthEventRecord(
                eventId = UUID.randomUUID().toString(),
                eventType = type.name,
                subject = subject,
                userId = userId,
                ipAddress = ipAddress,
                userAgent = userAgent,
                failureReason = failureReason,
                occurredAt = Instant.now(),
            )
        )
    }
}
