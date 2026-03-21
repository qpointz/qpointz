package io.qpointz.mill.persistence.security.jpa.entities

import jakarta.persistence.*
import java.time.Instant

/**
 * Canonical user record — the single authoritative row for every user in the system,
 * regardless of authentication method.
 *
 * Every authentication method (local, OAuth, PAT) resolves to this record through
 * [UserIdentityRecord]. The [userId] is a stable UUID assigned on first provisioning
 * and never changes, even when the user changes their email or username.
 *
 * @property userId stable UUID primary key — never changes after creation
 * @property status lifecycle status of the account (ACTIVE / DISABLED / LOCKED)
 * @property displayName optional human-readable display name
 * @property primaryEmail optional primary email address; may differ from profile email
 * @property createdAt timestamp when this record was first created
 * @property updatedAt timestamp of the most recent update to this record
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_primary_email", columnList = "primary_email"),
    ],
)
class UserRecord(
    @Id
    @Column(name = "user_id", nullable = false, length = 255)
    val userId: String,

    @Column(name = "status", nullable = false, length = 32)
    var status: String,

    @Column(name = "display_name", length = 512)
    var displayName: String?,

    @Column(name = "primary_email", length = 512)
    var primaryEmail: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
