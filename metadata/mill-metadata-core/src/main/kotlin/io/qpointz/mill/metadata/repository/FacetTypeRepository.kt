package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.FacetType

/** Runtime facet type rows (`metadata_facet_type`) — SPEC §6.3. */
interface FacetTypeRepository {
    fun findByKey(typeKey: String): FacetType?

    fun findAll(): List<FacetType>

    fun findDefined(): List<FacetType>

    fun findObserved(): List<FacetType>

    fun save(facetType: FacetType): FacetType

    fun delete(typeKey: String)
}
