package io.qpointz.mill.metadata.api.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/** REST DTO for a metadata operation audit entry (wire names aligned with entity/facet DTOs). */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class MetadataAuditRecordDto(
    val auditId: String,
    val operationType: String,
    @param:JsonProperty("entityUrn")
    @param:JsonAlias("entityId")
    val entityUrn: String?,
    @param:JsonProperty("facetTypeUrn")
    @param:JsonAlias("facetType")
    val facetTypeUrn: String?,
    @param:JsonProperty("scopeUrn")
    @param:JsonAlias("scopeKey")
    val scopeUrn: String?,
    val actorId: String,
    val occurredAt: Instant,
    val payloadBefore: String?,
    val payloadAfter: String?,
    val changeSummary: String?
)

