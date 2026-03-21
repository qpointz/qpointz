package io.qpointz.mill.persistence.security.jpa.entities

import jakarta.persistence.*
import java.time.Instant

/**
 * User profile — supplementary attributes for a canonical user.
 *
 * Created lazily on first access; the absence of a row means the user has not yet
 * filled in any profile data. The [userId] is a one-to-one FK to [UserRecord.userId].
 *
 * Theme preference is intentionally excluded: it is managed client-side via Mantine's
 * ThemeContext and does not require server-side persistence at this stage.
 *
 * New universal attributes are added as nullable columns via Flyway migrations.
 * Domain-specific attributes (e.g. AI preferences) belong in separate extension tables
 * in their respective persistence modules — see `docs/design/security/user-profile-extensibility.md`.
 *
 * @property userId FK to [UserRecord.userId] — also the primary key (one-to-one)
 * @property displayName optional human-readable display name (may differ from [UserRecord.displayName])
 * @property email optional email address stored in the profile
 * @property theme UI colour scheme preference: `"light"`, `"dark"`, or `"system"`
 * @property locale optional locale string (e.g. `"en-US"`)
 * @property updatedAt timestamp of the most recent update to this profile
 */
@Entity
@Table(name = "user_profiles")
class UserProfileRecord(
    @Id
    @Column(name = "user_id", nullable = false, length = 255)
    val userId: String,

    @Column(name = "display_name", length = 512)
    var displayName: String?,

    @Column(name = "email", length = 512)
    var email: String?,

    @Column(name = "theme", length = 32)
    var theme: String?,

    @Column(name = "locale", length = 64)
    var locale: String?,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
