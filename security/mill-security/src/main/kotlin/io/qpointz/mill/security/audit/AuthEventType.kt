package io.qpointz.mill.security.audit

/**
 * Enumeration of security-relevant authentication and account lifecycle events
 * recorded by the audit trail.
 *
 * Each value corresponds to a distinct category of event that may be written to
 * the `auth_events` table. Consumers should treat this list as open — new values
 * may be added in future releases without breaking existing serialised records.
 */
enum class AuthEventType {

    /**
     * A user completed authentication successfully.
     *
     * Written after the authentication manager accepts the supplied credentials and
     * a session (or token) is issued.
     */
    LOGIN_SUCCESS,

    /**
     * An authentication attempt was rejected.
     *
     * The `failureReason` field on the accompanying event record carries the
     * specific cause (e.g. `BAD_CREDENTIALS`, `USER_NOT_FOUND`).
     */
    LOGIN_FAILURE,

    /**
     * A user explicitly ended their session.
     *
     * Written when the logout endpoint invalidates the HTTP session.
     */
    LOGOUT,

    /**
     * A new user account was created successfully via the registration endpoint.
     */
    REGISTER_SUCCESS,

    /**
     * A registration attempt was rejected before an account was created.
     *
     * The `failureReason` field carries the cause (e.g. `REGISTRATION_DISABLED`,
     * `DUPLICATE_EMAIL`, `VALIDATION_ERROR`).
     */
    REGISTER_FAILURE,

    /**
     * An authenticated user's profile attributes were updated.
     *
     * Written after a successful `PATCH /auth/profile` call.
     */
    PROFILE_UPDATE,
}
