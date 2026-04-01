package io.qpointz.mill.persistence.metadata.jpa.adapters

import com.fasterxml.jackson.core.type.TypeReference
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityFacetEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeInstJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import io.qpointz.mill.utils.JsonUtils
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * @param facetJpa facet assignment rows
 * @param entityJpa entity FK resolution
 * @param typeJpa runtime facet type FK resolution
 * @param scopeJpa scope FK resolution
 */
@Transactional
class JpaFacetRepository(
    private val facetJpa: MetadataFacetJpaRepository,
    private val entityJpa: MetadataEntityJpaRepository,
    private val typeJpa: MetadataFacetTypeInstJpaRepository,
    private val scopeJpa: MetadataScopeJpaRepository
) : FacetRepository {

    private val mapper = JsonUtils.defaultJsonMapper()

    override fun findByEntity(entityId: String): List<FacetAssignment> {
        val res = MetadataEntityUrn.canonicalize(entityId)
        return facetJpa.findByEntityEntityRes(res).map { toDomain(it) }
    }

    override fun findByEntityAndType(entityId: String, facetTypeKey: String): List<FacetAssignment> {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        return facetJpa.findByEntityEntityRes(eid).filter { it.facetType.typeRes == tid }.map { toDomain(it) }
    }

    override fun findByEntityTypeAndScope(
        entityId: String,
        facetTypeKey: String,
        scopeKey: String
    ): List<FacetAssignment> {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        val sid = MetadataEntityUrn.canonicalize(scopeKey)
        return facetJpa.listByEntityResFacetTypeResScopeRes(eid, tid, sid).map { toDomain(it) }
    }

    override fun findByUid(uid: String): FacetAssignment? =
        facetJpa.findByUuid(uid).map { toDomain(it) }.orElse(null)

    override fun save(facet: FacetAssignment): FacetAssignment {
        val eid = MetadataEntityUrn.canonicalize(facet.entityId)
        val tid = MetadataEntityUrn.canonicalize(facet.facetTypeKey)
        val sid = MetadataEntityUrn.canonicalize(facet.scopeKey)
        val entity = entityJpa.findByEntityRes(eid).orElseThrow { IllegalArgumentException("Unknown entity: $eid") }
        val type = typeJpa.findByTypeRes(tid).orElseThrow { IllegalArgumentException("Unknown facet type: $tid") }
        val scope = scopeJpa.findByScopeRes(sid).orElseThrow { IllegalArgumentException("Unknown scope: $sid") }
        val payloadJson = mapper.writeValueAsString(facet.payload)
        val now = Instant.now()
        val row = facetJpa.findByUuid(facet.uid).orElse(null)
        val saved = if (row == null) {
            facetJpa.save(
                MetadataEntityFacetEntity(
                    uuid = facet.uid,
                    entity = entity,
                    facetType = type,
                    scope = scope,
                    payloadJson = payloadJson,
                    mergeAction = facet.mergeAction.name,
                    createdAt = now,
                    createdBy = facet.createdBy,
                    lastModifiedAt = now,
                    lastModifiedBy = facet.lastModifiedBy
                )
            )
        } else {
            row.payloadJson = payloadJson
            row.mergeAction = facet.mergeAction.name
            row.lastModifiedAt = now
            row.lastModifiedBy = facet.lastModifiedBy
            facetJpa.save(row)
        }
        return toDomain(saved)
    }

    override fun deleteByUid(uid: String): Boolean {
        val row = facetJpa.findByUuid(uid).orElse(null) ?: return false
        facetJpa.delete(row)
        return true
    }

    override fun deleteByEntity(entityId: String) {
        facetJpa.deleteByEntityEntityRes(MetadataEntityUrn.canonicalize(entityId))
    }

    override fun deleteByEntityTypeAndScope(entityId: String, facetTypeKey: String, scopeKey: String) {
        facetJpa.deleteByEntityResAndFacetTypeResAndScopeRes(
            MetadataEntityUrn.canonicalize(entityId),
            MetadataEntityUrn.canonicalize(facetTypeKey),
            MetadataEntityUrn.canonicalize(scopeKey)
        )
    }

    override fun countByFacetType(facetTypeKey: String): Int =
        facetJpa.countByFacetTypeTypeRes(MetadataEntityUrn.canonicalize(facetTypeKey)).toInt()

    private fun toDomain(e: MetadataEntityFacetEntity): FacetAssignment {
        val payload: Map<String, Any?> = mapper.readValue(
            e.payloadJson,
            object : TypeReference<Map<String, Any?>>() {}
        )
        return FacetAssignment(
            uid = e.uuid,
            entityId = e.entity.entityRes,
            facetTypeKey = e.facetType.typeRes,
            scopeKey = e.scope.scopeRes,
            mergeAction = MergeAction.valueOf(e.mergeAction),
            payload = payload,
            createdAt = e.createdAt,
            createdBy = e.createdBy,
            lastModifiedAt = e.lastModifiedAt,
            lastModifiedBy = e.lastModifiedBy
        )
    }
}
