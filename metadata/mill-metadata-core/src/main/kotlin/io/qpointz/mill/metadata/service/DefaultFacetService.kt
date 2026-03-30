package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetType
import io.qpointz.mill.metadata.domain.FacetTypeSource
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import java.time.Instant
import java.util.UUID

/**
 * @param facetRepository assignment persistence
 * @param facetCatalog cardinality and definition lookup
 * @param facetTypeRepository runtime type rows (OBSERVED auto-create)
 * @param metadataReader scope merge for [resolve] / [resolveByType]
 */
class DefaultFacetService(
    private val facetRepository: FacetRepository,
    private val facetCatalog: FacetCatalog,
    private val facetTypeRepository: FacetTypeRepository,
    private val metadataReader: MetadataReader
) : FacetService {

    override fun resolve(entityId: String, context: MetadataContext): List<FacetInstance> {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val all = facetRepository.findByEntity(eid)
        return metadataReader.resolveEffective(all, context)
    }

    override fun resolveByType(
        entityId: String,
        facetTypeKey: String,
        context: MetadataContext
    ): List<FacetInstance> {
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        return resolve(entityId, context).filter { it.facetTypeKey == tid }
    }

    override fun assign(
        entityId: String,
        facetTypeKey: String,
        scopeKey: String,
        payload: Map<String, Any?>,
        actor: String
    ): FacetInstance {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        val sid = MetadataEntityUrn.canonicalize(scopeKey)
        ensureObservedFacetType(tid, actor)
        val card = facetCatalog.resolveCardinality(tid)
        val now = Instant.now()
        if (card == FacetTargetCardinality.SINGLE) {
            val existing = facetRepository.findByEntityTypeAndScope(eid, tid, sid).firstOrNull()
            if (existing != null) {
                val updated = existing.copy(
                    payload = payload,
                    mergeAction = MergeAction.SET,
                    lastModifiedAt = now,
                    lastModifiedBy = actor
                )
                return facetRepository.save(updated)
            }
        }
        val uid = UUID.randomUUID().toString()
        val row = FacetInstance(
            uid = uid,
            entityId = eid,
            facetTypeKey = tid,
            scopeKey = sid,
            mergeAction = MergeAction.SET,
            payload = payload,
            createdAt = now,
            createdBy = actor,
            lastModifiedAt = now,
            lastModifiedBy = actor
        )
        return facetRepository.save(row)
    }

    override fun update(uid: String, payload: Map<String, Any?>, actor: String): FacetInstance {
        val row = facetRepository.findByUid(uid)
            ?: throw IllegalArgumentException("Unknown facet uid: $uid")
        val now = Instant.now()
        return facetRepository.save(
            row.copy(
                payload = payload,
                lastModifiedAt = now,
                lastModifiedBy = actor
            )
        )
    }

    override fun unassign(uid: String, actor: String): Boolean {
        val row = facetRepository.findByUid(uid) ?: return false
        val now = Instant.now()
        val canonScope = MetadataEntityUrn.canonicalize(row.scopeKey)
        val global = canonScope == MetadataUrns.SCOPE_GLOBAL
        return when {
            row.mergeAction == MergeAction.SET && global -> facetRepository.deleteByUid(uid)
            row.mergeAction == MergeAction.SET && !global -> {
                facetRepository.save(
                    row.copy(
                        mergeAction = MergeAction.TOMBSTONE,
                        lastModifiedAt = now,
                        lastModifiedBy = actor
                    )
                )
                true
            }
            else -> facetRepository.deleteByUid(uid)
        }
    }

    override fun unassignAll(entityId: String, facetTypeKey: String, scopeKey: String, actor: String) {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        val sid = MetadataEntityUrn.canonicalize(scopeKey)
        val rows = facetRepository.findByEntityTypeAndScope(eid, tid, sid).toList()
        for (r in rows) {
            unassign(r.uid, actor)
        }
    }

    private fun ensureObservedFacetType(typeKey: String, actor: String) {
        if (facetTypeRepository.findByKey(typeKey) != null) return
        val now = Instant.now()
        facetTypeRepository.save(
            FacetType(
                typeKey = typeKey,
                source = FacetTypeSource.OBSERVED,
                definition = null,
                createdAt = now,
                createdBy = actor,
                lastModifiedAt = now,
                lastModifiedBy = actor
            )
        )
    }
}
