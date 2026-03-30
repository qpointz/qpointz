package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.FacetTypeDefinition

/** Persistence for `metadata_facet_type_def` — SPEC §6.3. */
interface FacetTypeDefinitionRepository {
    fun findByKey(typeKey: String): FacetTypeDefinition?

    fun findAll(): List<FacetTypeDefinition>

    fun save(definition: FacetTypeDefinition): FacetTypeDefinition

    fun delete(typeKey: String)
}
