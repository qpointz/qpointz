package io.qpointz.mill.persistence.security.jpa.entities

import jakarta.persistence.*
import java.time.Instant

/**
 * Identity bridge record — maps a provider/subject pair to a canonical [UserRecord.userId].
 *
 * This is the primary lookup table for all authentication methods:
 * - Local/basic auth: `provider = "local"`, `subject = <username>`
 * - OAuth/OIDC: `provider = <registrationId>`, `subject = <OAuth sub claim>`
 * - Future PAT: `provider = "pat"`, `subject = <token id prefix>`
 *
 * The `(provider, subject)` pair has a unique constraint to prevent duplicate provisioning.
 *
 * @property identityId stable UUID primary key
 * @property provider authentication provider identifier (e.g. `"local"`, `"entra"`)
 * @property subject provider-specific user identifier (username, OAuth sub, etc.)
 * @property userId FK to [UserRecord.userId] — the resolved canonical user
 * @property claimsSnapshot optional JSON snapshot of OAuth claims at last login
 * @property createdAt timestamp when this identity was first registered
 * @property updatedAt timestamp of the most recent update
 */
@Entity
@Table(
    name = "user_identities",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_user_identities_provider_subject", columnNames = ["provider", "subject"]),
    ],
    indexes = [
        Index(name = "idx_user_identities_user_id", columnList = "user_id"),
    ],
)
class UserIdentityRecord(
    @Id
    @Column(name = "identity_id", nullable = false, length = 255)
    val identityId: String,

    @Column(name = "provider", nullable = false, length = 128)
    val provider: String,

    @Column(name = "subject", nullable = false, length = 512)
    val subject: String,

    @Column(name = "user_id", nullable = false, length = 255)
    val userId: String,

    @Column(name = "claims_snapshot", columnDefinition = "TEXT")
    var claimsSnapshot: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
