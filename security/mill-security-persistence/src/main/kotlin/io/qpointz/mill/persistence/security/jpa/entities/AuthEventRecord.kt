package io.qpointz.mill.persistence.security.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

/**
 * JPA entity representing a single security audit event stored in the `auth_events` table.
 *
 * Each row is immutable once written — no fields are declared `var`. The [eventId] is a
 * UUID assigned by the writing service before persistence.
 *
 * The [userId] column intentionally carries no foreign-key constraint so that failed login
 * attempts against unknown usernames can still be recorded without a matching row in `users`.
 *
 * @property eventId stable UUID primary key assigned at write time; never changes
 * @property eventType machine-readable event category (e.g. `LOGIN_SUCCESS`, `LOGIN_FAILURE`);
 *   values correspond to [io.qpointz.mill.security.audit.AuthEventType]
 * @property userId canonical user identifier from the `users` table; `null` when the subject
 *   could not be resolved to a known user (e.g. login attempt with an unknown username)
 * @property subject the username, email address, or other identity token presented during the
 *   operation; `null` if not available
 * @property ipAddress originating IP address of the HTTP request; `null` if unavailable
 * @property userAgent value of the `User-Agent` request header; `null` if not present
 * @property failureReason short machine-readable code explaining a rejection; `null` for
 *   success events (e.g. `BAD_CREDENTIALS`, `DUPLICATE_EMAIL`, `VALIDATION_ERROR`)
 * @property occurredAt timestamp when the event was recorded; set to `Instant.now()` by the
 *   writing service
 */
@Entity
@Table(
    name = "auth_events",
    indexes = [
        Index(name = "idx_auth_events_user_id",     columnList = "user_id"),
        Index(name = "idx_auth_events_event_type",  columnList = "event_type"),
        Index(name = "idx_auth_events_occurred_at", columnList = "occurred_at"),
    ],
)
class AuthEventRecord(

    @Id
    @Column(name = "event_id", nullable = false, length = 255)
    val eventId: String,

    @Column(name = "event_type", nullable = false, length = 64)
    val eventType: String,

    @Column(name = "user_id", length = 255)
    val userId: String?,

    @Column(name = "subject", length = 512)
    val subject: String?,

    @Column(name = "ip_address", length = 64)
    val ipAddress: String?,

    @Column(name = "user_agent", length = 1024)
    val userAgent: String?,

    @Column(name = "failure_reason", length = 255)
    val failureReason: String?,

    @Column(name = "occurred_at", nullable = false)
    val occurredAt: Instant,
)
