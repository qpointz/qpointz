package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataOperationAuditRecord

/** Fallback audit repository that returns no history rows. */
object NoOpMetadataOperationAuditRepository : MetadataOperationAuditRepository {
    override fun findByEntityId(entityId: String): List<MetadataOperationAuditRecord> = emptyList()
}

