package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.AuditEntry
import io.qpointz.mill.metadata.repository.MetadataAuditRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataAuditEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataAuditJpaRepository
import org.springframework.transaction.annotation.Transactional

/**
 * @param jpa Spring Data repository for `metadata_audit`
 */
@Transactional
class JpaMetadataAuditRepository(
    private val jpa: MetadataAuditJpaRepository
) : MetadataAuditRepository {

    override fun record(entry: AuditEntry) {
        jpa.save(
            MetadataAuditEntity(
                operation = entry.operation,
                subjectType = entry.subjectType,
                subjectRef = entry.subjectRef,
                actorId = entry.actorId,
                correlationId = entry.correlationId,
                occurredAt = entry.occurredAt,
                payloadBefore = entry.payloadBefore,
                payloadAfter = entry.payloadAfter
            )
        )
    }

    override fun findBySubjectRef(subjectRef: String): List<AuditEntry> =
        jpa.findBySubjectRef(subjectRef).map { toDomain(it) }

    override fun findByActor(actorId: String): List<AuditEntry> =
        jpa.findByActorId(actorId).map { toDomain(it) }

    private fun toDomain(e: MetadataAuditEntity): AuditEntry = AuditEntry(
        operation = e.operation,
        subjectType = e.subjectType,
        subjectRef = e.subjectRef,
        actorId = e.actorId,
        correlationId = e.correlationId,
        occurredAt = e.occurredAt,
        payloadBefore = e.payloadBefore,
        payloadAfter = e.payloadAfter
    )
}
