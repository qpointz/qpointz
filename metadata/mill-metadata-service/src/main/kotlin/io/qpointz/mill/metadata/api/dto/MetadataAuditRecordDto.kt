package io.qpointz.mill.metadata.api.dto

import java.time.Instant

/** REST DTO for a metadata operation audit entry. */
data class MetadataAuditRecordDto(
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

