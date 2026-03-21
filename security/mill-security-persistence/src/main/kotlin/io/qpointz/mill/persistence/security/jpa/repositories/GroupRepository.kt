package io.qpointz.mill.persistence.security.jpa.repositories

import io.qpointz.mill.persistence.security.jpa.entities.GroupRecord
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA repository for [GroupRecord] entities.
 */
interface GroupRepository : JpaRepository<GroupRecord, String>
