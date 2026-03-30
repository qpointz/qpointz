package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataAuditEntity
import org.springframework.data.jpa.repository.JpaRepository

/** Spring Data repository for [MetadataAuditEntity] (`metadata_audit`). */
interface MetadataAuditJpaRepository : JpaRepository<MetadataAuditEntity, Long> {

    fun findBySubjectRef(subjectRef: String): List<MetadataAuditEntity>

    fun findByActorId(actorId: String): List<MetadataAuditEntity>
}
