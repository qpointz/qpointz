package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataScope
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [MetadataScopeRepository] for file-backed and test compositions without JPA.
 *
 * @param byRes keys are canonical scope resource URNs ([MetadataEntityUrn.canonicalize] on [MetadataScope.res])
 */
class InMemoryMetadataScopeRepository : MetadataScopeRepository {

    private val byRes = ConcurrentHashMap<String, MetadataScope>()

    override fun findByRes(scopeRes: String): MetadataScope? =
        byRes[MetadataEntityUrn.canonicalize(scopeRes)]

    override fun findAll(): List<MetadataScope> =
        byRes.values.sortedBy { it.res }

    override fun findByType(scopeType: String): List<MetadataScope> {
        val t = scopeType.uppercase()
        return byRes.values.filter { it.scopeType.equals(t, ignoreCase = true) }
    }

    override fun exists(scopeRes: String): Boolean =
        byRes.containsKey(MetadataEntityUrn.canonicalize(scopeRes))

    override fun save(scope: MetadataScope): MetadataScope {
        val res = MetadataEntityUrn.canonicalize(scope.res)
        val row = scope.copy(res = res)
        byRes[res] = row
        return row
    }

    override fun delete(scopeRes: String) {
        byRes.remove(MetadataEntityUrn.canonicalize(scopeRes))
    }
}
