package io.qpointz.mill.data.metadata

/**
 * Facet-type URNs for flow-descriptor metadata contributions (`MetadataOriginIds.FLOW`).
 * Definitions are seeded from `metadata/platform-flow-facet-types.yaml`.
 */
object FlowInferredFacetTypeKeys {
    const val SCHEMA: String = "urn:mill/metadata/facet-type:flow-schema"
    const val TABLE: String = "urn:mill/metadata/facet-type:flow-table"
    const val COLUMN: String = "urn:mill/metadata/facet-type:flow-column"
}
