package io.qpointz.mill.persistence.security.jpa.repositories

import io.qpointz.mill.persistence.security.jpa.entities.GroupMembershipKey
import io.qpointz.mill.persistence.security.jpa.entities.GroupMembershipRecord
import io.qpointz.mill.persistence.security.jpa.entities.GroupRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Spring Data JPA repository for [GroupMembershipRecord] entities.
 */
interface GroupMembershipRepository : JpaRepository<GroupMembershipRecord, GroupMembershipKey> {

    /**
     * Finds all groups that the given user belongs to.
     *
     * @param userId canonical [UserRecord.userId]
     * @return list of [GroupRecord] entities the user is a member of
     */
    @Query("SELECT g FROM GroupRecord g JOIN GroupMembershipRecord m ON g.groupId = m.id.groupId WHERE m.id.userId = :userId")
    fun findGroupsByUserId(userId: String): List<GroupRecord>
}
