package io.qpointz.mill.data.schema

/**
 * Aggregation boundary that merges physical schema with schema-bound metadata.
 *
 * Implementations must preserve all physical entities exposed by the underlying
 * [io.qpointz.mill.data.backend.SchemaProvider] regardless of metadata coverage.
 */
interface SchemaFacetService {
    /**
     * Returns all physical schemas merged with schema-bound metadata.
     *
     * Every schema, table, and attribute present in the underlying [io.qpointz.mill.data.backend.SchemaProvider]
     * is guaranteed to appear in the result regardless of metadata coverage.
     * Metadata entities that could not be matched to any physical coordinate are
     * collected in [SchemaFacetResult.unboundMetadata].
     */
    fun getSchemas(): SchemaFacetResult
}
