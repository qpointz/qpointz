package io.qpointz.mill.metadata.service

import io.qpointz.mill.data.schema.MetadataEntityUrnCodec
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.MetadataAuditRepository
import java.util.UUID

/**
 * @param entityService entity identity CRUD
 * @param facetService assignment operations with catalog rules
 * @param facetRepository used for cascade queries and history aggregation
 * @param auditRepository optional audit trail (no-op outside JPA)
 * @param urnCodec canonical entity id resolution for legacy keys
 */
class DefaultMetadataEditService(
    private val entityService: MetadataEntityService,
    private val facetService: FacetService,
    private val facetRepository: FacetRepository,
    private val auditRepository: MetadataAuditRepository,
    private val urnCodec: MetadataEntityUrnCodec
) : MetadataEditService {

    private fun cid(raw: String): String = MetadataEntityIdResolver.resolve(raw, urnCodec)

    override fun createEntity(entity: MetadataEntity, actor: String): MetadataEntity =
        entityService.create(entity, actor)

    override fun overwriteEntity(id: String, entity: MetadataEntity, actor: String): MetadataEntity {
        val entityId = cid(id)
        val existing = entityService.findById(entityId)
            ?: throw IllegalArgumentException("Unknown entity: $entityId")
        facetRepository.deleteByEntity(entityId)
        return entityService.update(
            entity.copy(
                id = entityId,
                uuid = existing.uuid,
                createdAt = existing.createdAt,
                createdBy = existing.createdBy
            ),
            actor
        )
    }

    override fun deleteEntity(id: String, actor: String) {
        entityService.delete(cid(id), actor)
    }

    override fun setFacet(
        id: String,
        typeKey: String,
        scope: String,
        payload: Any?,
        actor: String
    ): FacetInstance {
        val entityId = cid(id)
        val tid = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(typeKey))
        val sid = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseScopePath(scope))
        return facetService.assign(entityId, tid, sid, FacetPayloadCoercion.toPayloadMap(payload), actor)
    }

    override fun deleteFacet(id: String, typeKey: String, scope: String, actor: String) {
        val entityId = cid(id)
        val tid = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(typeKey))
        val sid = MetadataEntityUrn.canonicalize(MetadataUrns.normaliseScopePath(scope))
        facetService.unassignAll(entityId, tid, sid, actor)
    }

    override fun deleteFacetInstanceByUid(id: String, facetUid: String, actor: String) {
        val entityId = cid(id)
        val row = facetRepository.findByUid(facetUid.trim())
            ?: throw IllegalArgumentException("Unknown facet uid: $facetUid")
        require(row.entityId == entityId) {
            "Facet instance $facetUid does not belong to entity $entityId"
        }
        facetService.unassign(facetUid.trim(), actor)
    }

    override fun history(id: String): List<MetadataHistoryRecord> {
        val entityId = cid(id)
        val entries = mutableListOf<io.qpointz.mill.metadata.domain.AuditEntry>()
        entries += auditRepository.findBySubjectRef(entityId)
        for (facet in facetRepository.findByEntity(entityId)) {
            entries += auditRepository.findBySubjectRef(facet.uid)
        }
        return entries
            .distinctBy { listOf(it.operation, it.subjectRef, it.occurredAt, it.payloadAfter) }
            .sortedBy { it.occurredAt }
            .map { toHistory(it) }
    }

    private fun toHistory(e: io.qpointz.mill.metadata.domain.AuditEntry): MetadataHistoryRecord {
        val stable = "${e.operation}|${e.occurredAt}|${e.subjectRef}|${e.actorId}"
        val hid = UUID.nameUUIDFromBytes(stable.toByteArray(Charsets.UTF_8)).toString()
        return MetadataHistoryRecord(
            auditId = hid,
            operationType = e.operation,
            entityId = when (e.subjectType) {
                "ENTITY" -> e.subjectRef
                else -> null
            },
            facetType = null,
            scopeKey = null,
            actorId = e.actorId.orEmpty(),
            occurredAt = e.occurredAt,
            payloadBefore = e.payloadBefore,
            payloadAfter = e.payloadAfter,
            changeSummary = null
        )
    }
}
