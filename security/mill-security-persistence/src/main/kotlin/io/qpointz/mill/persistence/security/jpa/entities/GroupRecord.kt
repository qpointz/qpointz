package io.qpointz.mill.persistence.security.jpa.entities

import jakarta.persistence.*

/**
 * Named group that users can belong to.
 *
 * Groups are used to assign [GrantedAuthority] values in Spring Security.
 * The group name is the authority string returned by
 * [JpaUserIdentityResolutionService.loadAuthorities].
 *
 * @property groupId stable UUID primary key
 * @property groupName unique human-readable name for the group (used as authority)
 * @property description optional description of the group's purpose
 */
@Entity
@Table(
    name = "groups",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_groups_group_name", columnNames = ["group_name"]),
    ],
)
class GroupRecord(
    @Id
    @Column(name = "group_id", nullable = false, length = 255)
    val groupId: String,

    @Column(name = "group_name", nullable = false, length = 255)
    val groupName: String,

    @Column(name = "description", length = 1024)
    val description: String?,
)
