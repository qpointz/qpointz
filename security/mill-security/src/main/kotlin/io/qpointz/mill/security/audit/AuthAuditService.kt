package io.qpointz.mill.security.audit

/**
 * Contract for recording security audit events.
 *
 * Implementations persist an immutable record of each authentication and account
 * lifecycle event so that operators can investigate incidents and meet compliance
 * requirements. All parameters except [type] are nullable to support scenarios
 * where the information is unavailable — for example, a failed login attempt for
 * an unknown username has no resolvable [userId].
 *
 * The service is always injected as optional (`null`) into controllers and handlers
 * so that deployments without a persistence module continue to function without
 * audit writes.
 */
interface AuthAuditService {

    /**
     * Records a single authentication or account lifecycle event.
     *
     * The implementation is expected to persist the event immediately and
     * durably. Callers should not assume any retry behaviour on write failure.
     *
     * @param type the category of event being recorded; never `null`
     * @param subject the username, email address, or other identity token that was
     *   presented during the operation; may be `null` when the subject is unknown
     * @param userId the canonical internal user identifier resolved for this event;
     *   may be `null` when the subject could not be resolved (e.g. failed login with
     *   an unknown username)
     * @param ipAddress the originating IP address of the request; may be `null` if
     *   not available from the transport layer
     * @param userAgent the `User-Agent` header value from the request; may be `null`
     *   if not present
     * @param failureReason a short machine-readable code describing why the operation
     *   was rejected; `null` for success events. Typical values:
     *   `BAD_CREDENTIALS`, `USER_NOT_FOUND`, `REGISTRATION_DISABLED`,
     *   `DUPLICATE_EMAIL`, `VALIDATION_ERROR`
     */
    fun record(
        type: AuthEventType,
        subject: String?,
        userId: String?,
        ipAddress: String?,
        userAgent: String?,
        failureReason: String? = null,
    )
}
