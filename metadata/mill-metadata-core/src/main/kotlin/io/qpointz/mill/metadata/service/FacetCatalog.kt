package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetType
import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.ValidationResult
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality

/** Facet type catalog (SPEC §7.3). */
interface FacetCatalog {
    fun findType(typeKey: String): FacetType?
    fun findDefinition(typeKey: String): FacetTypeDefinition?
    fun listDefinitions(): List<FacetTypeDefinition>
    fun listTypes(): List<FacetType>
    fun inspect(typeKey: String, payload: Map<String, Any?>): ValidationResult
    fun resolveCardinality(typeKey: String): FacetTargetCardinality
    fun registerDefinition(definition: FacetTypeDefinition): FacetTypeDefinition
}
