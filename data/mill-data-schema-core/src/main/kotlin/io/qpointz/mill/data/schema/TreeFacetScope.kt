package io.qpointz.mill.data.schema

/**
 * Controls how much facet resolution [SchemaFacetService.getSchemaTree] performs.
 *
 * Tree responses never include column nodes; scopes limit facet merge to model/schema/table
 * summaries only.
 */
enum class TreeFacetScope {
    /** Physical hierarchy only; no facet merge. */
    NONE,

    /** Facets on model root and schema nodes only. */
    DIRECT,

    /** Facets on model root, schema, and table summaries. */
    HIERARCHY
}
