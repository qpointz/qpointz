package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [FacetRepository] for tests and local bootstraps without JPA.
 */
class InMemoryFacetRepository : FacetRepository {

    private val byUid = ConcurrentHashMap<String, FacetInstance>()

    override fun findByEntity(entityId: String): List<FacetInstance> {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        return byUid.values.filter { MetadataEntityUrn.canonicalize(it.entityId) == eid }
    }

    override fun findByEntityAndType(entityId: String, facetTypeKey: String): List<FacetInstance> {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        return byUid.values.filter {
            MetadataEntityUrn.canonicalize(it.entityId) == eid &&
                MetadataEntityUrn.canonicalize(it.facetTypeKey) == tid
        }
    }

    override fun findByEntityTypeAndScope(
        entityId: String,
        facetTypeKey: String,
        scopeKey: String
    ): List<FacetInstance> {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        val sid = MetadataEntityUrn.canonicalize(scopeKey)
        return byUid.values.filter {
            MetadataEntityUrn.canonicalize(it.entityId) == eid &&
                MetadataEntityUrn.canonicalize(it.facetTypeKey) == tid &&
                MetadataEntityUrn.canonicalize(it.scopeKey) == sid
        }
    }

    override fun findByUid(uid: String): FacetInstance? = byUid[uid]

    override fun save(facet: FacetInstance): FacetInstance {
        byUid[facet.uid] = facet
        return facet
    }

    override fun deleteByUid(uid: String): Boolean = byUid.remove(uid) != null

    override fun deleteByEntity(entityId: String) {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        byUid.entries.removeIf { MetadataEntityUrn.canonicalize(it.value.entityId) == eid }
    }

    override fun deleteByEntityTypeAndScope(entityId: String, facetTypeKey: String, scopeKey: String) {
        val eid = MetadataEntityUrn.canonicalize(entityId)
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        val sid = MetadataEntityUrn.canonicalize(scopeKey)
        byUid.entries.removeIf {
            val f = it.value
            MetadataEntityUrn.canonicalize(f.entityId) == eid &&
                MetadataEntityUrn.canonicalize(f.facetTypeKey) == tid &&
                MetadataEntityUrn.canonicalize(f.scopeKey) == sid
        }
    }

    override fun countByFacetType(facetTypeKey: String): Int {
        val tid = MetadataEntityUrn.canonicalize(facetTypeKey)
        return byUid.values.count { MetadataEntityUrn.canonicalize(it.facetTypeKey) == tid }
    }
}
