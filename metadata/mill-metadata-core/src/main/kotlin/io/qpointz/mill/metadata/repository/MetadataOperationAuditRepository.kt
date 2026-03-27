package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataOperationAuditRecord

/** Read-only access to metadata operation audit history. */
interface MetadataOperationAuditRepository {
    /**
     * Returns entity audit rows ordered by occurredAt descending.
     */
    fun findByEntityId(entityId: String): List<MetadataOperationAuditRecord>
}

