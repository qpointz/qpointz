package io.qpointz.mill.persistence.metadata.jpa.adapters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.MetadataChangeEvent
import io.qpointz.mill.metadata.domain.MetadataChangeObserverDelegate
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataOperationAuditEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataOperationAuditJpaRepository
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * [MetadataChangeObserverDelegate] that persists each [MetadataChangeEvent] as an immutable
 * row in `metadata_operation_audit`.
 */
class JpaMetadataChangeObserver(
    private val auditRepo: MetadataOperationAuditJpaRepository
) : MetadataChangeObserverDelegate {

    private val mapper: ObjectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
        disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun onEvent(event: MetadataChangeEvent) {
        try {
            val entry = toAuditEntry(event)
            auditRepo.save(entry)
            log.info("Audit entry persisted: operation={}, entityRes={}, actor={}",
                entry.operationType, entry.entityRes, entry.actorId)
        } catch (e: Exception) {
            log.warn("Failed to persist audit entry for event {}: {}",
                event::class.simpleName, e.message)
        }
    }

    private fun toAuditEntry(event: MetadataChangeEvent): MetadataOperationAuditEntity =
        when (event) {
            is MetadataChangeEvent.EntityCreated -> MetadataOperationAuditEntity(
                auditId = UUID.randomUUID().toString(),
                operationType = "EntityCreated",
                entityId = null,
                entityRes = event.entityId,
                facetTypeRes = null,
                scopeRes = null,
                actorId = event.actorId,
                occurredAt = event.occurredAt,
                payloadBefore = null,
                payloadAfter = toJson(event.entity),
                changeSummary = "Entity created: ${event.entityId}"
            )
            is MetadataChangeEvent.EntityUpdated -> MetadataOperationAuditEntity(
                auditId = UUID.randomUUID().toString(),
                operationType = "EntityUpdated",
                entityId = null,
                entityRes = event.entityId,
                facetTypeRes = null,
                scopeRes = null,
                actorId = event.actorId,
                occurredAt = event.occurredAt,
                payloadBefore = toJson(event.before),
                payloadAfter = toJson(event.after),
                changeSummary = "Entity updated: ${event.entityId}"
            )
            is MetadataChangeEvent.EntityDeleted -> MetadataOperationAuditEntity(
                auditId = UUID.randomUUID().toString(),
                operationType = "EntityDeleted",
                entityId = null,
                entityRes = event.entityId,
                facetTypeRes = null,
                scopeRes = null,
                actorId = event.actorId,
                occurredAt = event.occurredAt,
                payloadBefore = toJson(event.entity),
                payloadAfter = null,
                changeSummary = "Entity deleted: ${event.entityId}"
            )
            is MetadataChangeEvent.FacetUpdated -> MetadataOperationAuditEntity(
                auditId = UUID.randomUUID().toString(),
                operationType = "FacetUpdated",
                entityId = null,
                entityRes = event.entityId,
                facetTypeRes = event.facetType,
                scopeRes = event.scopeKey,
                actorId = event.actorId,
                occurredAt = event.occurredAt,
                payloadBefore = event.before?.let { toJson(it) },
                payloadAfter = event.after?.let { toJson(it) },
                changeSummary = "Facet ${event.facetType}@${event.scopeKey} updated on ${event.entityId}"
            )
            is MetadataChangeEvent.FacetDeleted -> MetadataOperationAuditEntity(
                auditId = UUID.randomUUID().toString(),
                operationType = "FacetDeleted",
                entityId = null,
                entityRes = event.entityId,
                facetTypeRes = event.facetType,
                scopeRes = event.scopeKey,
                actorId = event.actorId,
                occurredAt = event.occurredAt,
                payloadBefore = event.payload?.let { toJson(it) },
                payloadAfter = null,
                changeSummary = "Facet ${event.facetType}@${event.scopeKey} deleted on ${event.entityId}"
            )
            is MetadataChangeEvent.Imported -> MetadataOperationAuditEntity(
                auditId = UUID.randomUUID().toString(),
                operationType = "Imported",
                entityId = null,
                entityRes = event.entityId,
                facetTypeRes = null,
                scopeRes = null,
                actorId = event.actorId,
                occurredAt = event.occurredAt,
                payloadBefore = null,
                payloadAfter = toJson(event.entity),
                changeSummary = "Entity imported (${event.mode}): ${event.entityId}"
            )
        }

    private fun toJson(value: Any): String = mapper.writeValueAsString(value)

    companion object {
        private val log = LoggerFactory.getLogger(JpaMetadataChangeObserver::class.java)
    }
}
