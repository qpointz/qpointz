package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.MetadataOperationAuditRecord
import io.qpointz.mill.metadata.repository.MetadataOperationAuditRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataOperationAuditJpaRepository

/** JPA-backed audit history reader. */
class JpaMetadataOperationAuditRepository(
    private val jpaRepository: MetadataOperationAuditJpaRepository
) : MetadataOperationAuditRepository {
    override fun findByEntityId(entityId: String): List<MetadataOperationAuditRecord> =
        jpaRepository.findByEntityRes(entityId)
            .sortedByDescending { it.occurredAt }
            .map {
                MetadataOperationAuditRecord(
                    auditId = it.auditId,
                    operationType = it.operationType,
                    entityId = it.entityRes,
                    facetType = it.facetTypeRes,
                    scopeKey = it.scopeRes,
                    actorId = it.actorId,
                    occurredAt = it.occurredAt,
                    payloadBefore = it.payloadBefore,
                    payloadAfter = it.payloadAfter,
                    changeSummary = it.changeSummary
                )
            }
}
