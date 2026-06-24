package io.qpointz.mill.data.odata.resolve

import io.qpointz.mill.data.schema.SchemaTableWithFacets

/**
 * Optional cache for merged table facet metadata used during OData query composition.
 */
fun interface SchemaTableCache {

    /**
     * @param key stable cache key for schema, table, and metadata scope
     * @param loader supplier when the key is absent
     * @return cached or freshly loaded table metadata (may be null when unknown)
     */
    fun get(key: String, loader: () -> SchemaTableWithFacets?): SchemaTableWithFacets?
}
