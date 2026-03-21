package io.qpointz.mill.persistence.security.jpa.entities

import jakarta.persistence.*
import java.time.Instant

/**
 * Local password credential for a user.
 *
 * Only populated for local/basic-auth users. OAuth users have no row in this table.
 * The [passwordHash] is stored in Spring Security's `{prefix}hash` format so that
 * [org.springframework.security.crypto.password.DelegatingPasswordEncoder] can verify
 * credentials using the algorithm identified by [algorithm].
 *
 * @property credentialId stable UUID primary key
 * @property userId FK to [UserRecord.userId] — the owning user
 * @property passwordHash hashed password in `{algorithmId}hash` format
 * @property algorithm identifier for the hashing algorithm (e.g. `"noop"`, `"bcrypt"`)
 * @property enabled whether this credential is active; disabled credentials are rejected
 * @property createdAt timestamp when the credential was created
 * @property updatedAt timestamp of the most recent update
 */
@Entity
@Table(
    name = "user_credentials",
    indexes = [
        Index(name = "idx_user_credentials_user_id", columnList = "user_id"),
    ],
)
class UserCredentialRecord(
    @Id
    @Column(name = "credential_id", nullable = false, length = 255)
    val credentialId: String,

    @Column(name = "user_id", nullable = false, length = 255)
    val userId: String,

    @Column(name = "password_hash", nullable = false, length = 1024)
    var passwordHash: String,

    @Column(name = "algorithm", nullable = false, length = 64)
    var algorithm: String,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
