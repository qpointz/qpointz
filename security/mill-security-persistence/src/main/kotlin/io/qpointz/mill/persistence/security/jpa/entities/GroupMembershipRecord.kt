package io.qpointz.mill.persistence.security.jpa.entities

import jakarta.persistence.*
import java.io.Serializable

/**
 * Embeddable composite primary key for [GroupMembershipRecord].
 *
 * @property userId FK to [UserRecord.userId]
 * @property groupId FK to [GroupRecord.groupId]
 */
@Embeddable
data class GroupMembershipKey(
    @Column(name = "user_id", nullable = false, length = 255)
    val userId: String = "",

    @Column(name = "group_id", nullable = false, length = 255)
    val groupId: String = "",
) : Serializable

/**
 * Join table recording which groups a user belongs to.
 *
 * The composite primary key `(user_id, group_id)` enforces uniqueness — a user
 * may not belong to the same group more than once.
 *
 * @property id composite primary key embedding [userId] and [groupId]
 */
@Entity
@Table(
    name = "group_memberships",
    indexes = [
        Index(name = "idx_group_memberships_group_id", columnList = "group_id"),
    ],
)
class GroupMembershipRecord(
    @EmbeddedId
    val id: GroupMembershipKey,
)
