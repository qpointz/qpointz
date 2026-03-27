package io.qpointz.mill.metadata.service

import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.MetadataChangeEvent
import io.qpointz.mill.metadata.domain.MetadataChangeObserver
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataOperationAuditRecord
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataOperationAuditRepository
import io.qpointz.mill.metadata.repository.MetadataRepository
import java.time.Instant

/** Write service for metadata entities and facet payloads. */
class MetadataEditService(
    private val repository: MetadataRepository,
    private val metadataService: MetadataService,
    private val observer: MetadataChangeObserver,
    private val auditRepository: MetadataOperationAuditRepository,
) {
    fun createEntity(entity: MetadataEntity, actorId: String): MetadataEntity {
        val id = entity.id?.trim().orEmpty()
        if (id.isBlank()) throw MillStatuses.badRequestRuntime("Entity id is required")
        if (repository.existsById(id)) throw MillStatuses.conflictRuntime("Entity already exists: $id")
        val now = Instant.now()
        entity.createdAt = now
        entity.updatedAt = now
        entity.createdBy = actorId
        entity.updatedBy = actorId
        metadataService.save(entity)
        observer.onEvent(MetadataChangeEvent.EntityCreated(id, actorId, now, entity))
        return entity
    }

    fun overwriteEntity(id: String, replacement: MetadataEntity, actorId: String): MetadataEntity {
        val existing = repository.findById(id).orElseThrow {
            MillStatuses.notFoundRuntime("Entity not found: $id")
        }
        val now = Instant.now()
        replacement.id = id
        replacement.createdAt = existing.createdAt ?: now
        replacement.createdBy = existing.createdBy ?: actorId
        replacement.updatedAt = now
        replacement.updatedBy = actorId
        metadataService.save(replacement)
        observer.onEvent(MetadataChangeEvent.EntityUpdated(id, actorId, now, existing, replacement))
        return replacement
    }

    fun deleteEntity(id: String, actorId: String) {
        val existing = repository.findById(id).orElseThrow {
            MillStatuses.notFoundRuntime("Entity not found: $id")
        }
        metadataService.deleteById(id)
        observer.onEvent(MetadataChangeEvent.EntityDeleted(id, actorId, Instant.now(), existing))
    }

    fun setFacet(entityId: String, facetType: String, scopeKey: String, data: Any?, actorId: String): MetadataEntity {
        val entity = repository.findById(entityId).orElseThrow {
            MillStatuses.notFoundRuntime("Entity not found: $entityId")
        }
        val normalizedType = MetadataUrns.normaliseFacetTypePath(facetType)
        val normalizedScope = MetadataUrns.normaliseScopePath(scopeKey)
        val before = entity.getRawFacet(normalizedType, normalizedScope)
        entity.setFacet(normalizedType, normalizedScope, data)
        entity.updatedAt = Instant.now()
        entity.updatedBy = actorId
        metadataService.save(entity)
        observer.onEvent(
            MetadataChangeEvent.FacetUpdated(
                entityId,
                actorId,
                Instant.now(),
                normalizedType,
                normalizedScope,
                before,
                data
            )
        )
        return entity
    }

    fun deleteFacet(entityId: String, facetType: String, scopeKey: String, actorId: String) {
        val entity = repository.findById(entityId).orElseThrow {
            MillStatuses.notFoundRuntime("Entity not found: $entityId")
        }
        val normalizedType = MetadataUrns.normaliseFacetTypePath(facetType)
        val normalizedScope = MetadataUrns.normaliseScopePath(scopeKey)
        val facetMap = entity.facets[normalizedType] ?: throw MillStatuses.notFoundRuntime(
            "Facet type not found on entity: $normalizedType"
        )
        val before = facetMap.remove(normalizedScope)
            ?: throw MillStatuses.notFoundRuntime("Facet scope not found: $normalizedScope")
        if (facetMap.isEmpty()) {
            entity.facets.remove(normalizedType)
        }
        entity.updatedAt = Instant.now()
        entity.updatedBy = actorId
        metadataService.save(entity)
        observer.onEvent(
            MetadataChangeEvent.FacetDeleted(
                entityId,
                actorId,
                Instant.now(),
                normalizedType,
                normalizedScope,
                before
            )
        )
    }

    /**
     * Deletes a single facet instance row by [facetUid] (JPA). Reloads the entity, updates audit
     * timestamps, and emits [MetadataChangeEvent.FacetUpdated] or [MetadataChangeEvent.FacetDeleted].
     *
     * @param entityId business entity id (`entity_res`)
     * @param facetUid unique facet row id from the API
     * @param actorId  authenticated actor
     * @return the entity after persistence
     */
    fun deleteFacetInstanceByUid(entityId: String, facetUid: String, actorId: String): MetadataEntity {
        val row = repository.findFacetInstanceRow(entityId, facetUid)
            ?: throw MillStatuses.notFoundRuntime("Facet instance not found: $facetUid")
        val normalizedType = MetadataUrns.normaliseFacetTypePath(row.facetTypeKey)
        val normalizedScope = MetadataUrns.normaliseScopePath(row.scopeKey)
        val beforeEntity = repository.findById(entityId).orElseThrow {
            MillStatuses.notFoundRuntime("Entity not found: $entityId")
        }
        val beforePayload = beforeEntity.getRawFacet(normalizedType, normalizedScope)
        if (!repository.deleteFacetRowByUid(entityId, facetUid)) {
            throw MillStatuses.notFoundRuntime("Facet instance not found: $facetUid")
        }
        val afterEntity = repository.findById(entityId).orElseThrow {
            MillStatuses.notFoundRuntime("Entity not found: $entityId")
        }
        val afterPayload = afterEntity.getRawFacet(normalizedType, normalizedScope)
        afterEntity.updatedAt = Instant.now()
        afterEntity.updatedBy = actorId
        metadataService.save(afterEntity)
        val now = Instant.now()
        if (afterPayload == null) {
            observer.onEvent(
                MetadataChangeEvent.FacetDeleted(
                    entityId,
                    actorId,
                    now,
                    normalizedType,
                    normalizedScope,
                    beforePayload
                )
            )
        } else {
            observer.onEvent(
                MetadataChangeEvent.FacetUpdated(
                    entityId,
                    actorId,
                    now,
                    normalizedType,
                    normalizedScope,
                    beforePayload,
                    afterPayload
                )
            )
        }
        return afterEntity
    }

    fun history(entityId: String): List<MetadataOperationAuditRecord> =
        auditRepository.findByEntityId(entityId)
}

