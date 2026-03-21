package io.qpointz.mill.security.domain

/**
 * Lifecycle status of a canonical [ResolvedUser].
 *
 * - [ACTIVE] — normal operation; the user may authenticate and access resources.
 * - [DISABLED] — administratively disabled; authentication will be rejected.
 * - [LOCKED] — locked due to policy (e.g. too many failed login attempts); authentication rejected.
 */
enum class UserStatus {
    /** User account is active and may authenticate. */
    ACTIVE,

    /** User account has been administratively disabled. */
    DISABLED,

    /** User account has been locked, typically due to security policy. */
    LOCKED,
}
