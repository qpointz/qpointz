package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [FacetTypeDefinitionRepository] for tests and non-JPA bootstraps.
 *
 * Keys are canonical facet-type URNs ([MetadataEntityUrn.canonicalize]).
 */
class InMemoryFacetTypeDefinitionRepository : FacetTypeDefinitionRepository {

    private val byKey = ConcurrentHashMap<String, FacetTypeDefinition>()

    override fun findByKey(typeKey: String): FacetTypeDefinition? =
        byKey[MetadataEntityUrn.canonicalize(typeKey)]

    override fun findAll(): List<FacetTypeDefinition> = byKey.values.sortedBy { it.typeKey }

    override fun save(definition: FacetTypeDefinition): FacetTypeDefinition {
        val k = MetadataEntityUrn.canonicalize(definition.typeKey)
        val row = definition.copy(typeKey = k)
        byKey[k] = row
        return row
    }

    override fun delete(typeKey: String) {
        byKey.remove(MetadataEntityUrn.canonicalize(typeKey))
    }
}
