package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.FacetType
import io.qpointz.mill.metadata.domain.FacetTypeSource
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [FacetTypeRepository] for tests and non-JPA bootstraps.
 *
 * Keys are canonical facet-type URNs ([MetadataEntityUrn.canonicalize]).
 */
class InMemoryFacetTypeRepository : FacetTypeRepository {

    private val byKey = ConcurrentHashMap<String, FacetType>()

    override fun findByKey(typeKey: String): FacetType? =
        byKey[MetadataEntityUrn.canonicalize(typeKey)]

    override fun findAll(): List<FacetType> = byKey.values.sortedBy { it.typeKey }

    override fun findDefined(): List<FacetType> =
        findAll().filter { it.source == FacetTypeSource.DEFINED }

    override fun findObserved(): List<FacetType> =
        findAll().filter { it.source == FacetTypeSource.OBSERVED }

    override fun save(facetType: FacetType): FacetType {
        val k = MetadataEntityUrn.canonicalize(facetType.typeKey)
        val row = facetType.copy(typeKey = k)
        byKey[k] = row
        return row
    }

    override fun delete(typeKey: String) {
        byKey.remove(MetadataEntityUrn.canonicalize(typeKey))
    }
}
