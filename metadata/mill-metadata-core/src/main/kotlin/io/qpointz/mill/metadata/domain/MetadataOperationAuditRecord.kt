package io.qpointz.mill.metadata.domain

import java.time.Instant

/** Read model for metadata operation audit history entries. */
data class MetadataOperationAuditRecord(
    val auditId: String,
    val operationType: String,
    val entityId: String?,
    val facetType: String?,
    val scopeKey: String?,
    val actorId: String,
    val occurredAt: Instant,
    val payloadBefore: String?,
    val payloadAfter: String?,
    val changeSummary: String?
)

