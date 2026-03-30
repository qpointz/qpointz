package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.FacetTypeDefinition

/** No-op [FacetTypeDefinitionRepository]. */
class NoOpFacetTypeDefinitionRepository : FacetTypeDefinitionRepository {
    override fun findByKey(typeKey: String): FacetTypeDefinition? = null
    override fun findAll(): List<FacetTypeDefinition> = emptyList()
    override fun save(definition: FacetTypeDefinition): FacetTypeDefinition = definition
    override fun delete(typeKey: String) = Unit
}
