package io.qpointz.mill.persistence.metadata.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * JPA entity representing a metadata scope row in `metadata_scope`.
 *
 * The scope is the namespace under which facet data is stored. Platform scopes include the
 * global scope (`urn:mill/metadata/scope:global`). User, team, and role scopes are created
 * on demand during import or facet write operations.
 *
 * @property scopeId     Surrogate primary key.
 * @property scopeRes    Full Mill scope URN (former `scope_id` string), e.g. `"urn:mill/metadata/scope:user:alice"`.
 * @property scopeType   Coarse category: `GLOBAL`, `USER`, `TEAM`, `ROLE`
 * @property referenceId The local part of the scope URN; `null` for the global scope
 * @property displayName Optional human-readable label for the scope
 * @property ownerId     Optional identifier of the user who owns this scope
 * @property visibility  Visibility hint; default `PUBLIC`
 * @property createdAt   Timestamp when this scope row was first created
 */
@Entity
@Table(
    name = "metadata_scope",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_metadata_scope_type_ref", columnNames = ["scope_type", "reference_id"])
    ]
)
class MetadataScopeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scope_id", nullable = false)
    val scopeId: Long = 0,

    @Column(name = "scope_res", nullable = false, length = 255)
    var scopeRes: String,

    @Column(name = "scope_type", nullable = false, length = 32)
    var scopeType: String,

    @Column(name = "reference_id", length = 255)
    var referenceId: String?,

    @Column(name = "display_name", length = 512)
    var displayName: String?,

    @Column(name = "owner_id", length = 255)
    var ownerId: String?,

    @Column(name = "visibility", nullable = false, length = 32)
    var visibility: String = "PUBLIC",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant
)
