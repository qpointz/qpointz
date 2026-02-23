package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.ValidationResult

/** Validates facet payloads against descriptor-provided content constraints. */
interface FacetContentValidator {
    fun validate(contentSchema: Map<String, Any?>, facetData: Any?): ValidationResult
}
