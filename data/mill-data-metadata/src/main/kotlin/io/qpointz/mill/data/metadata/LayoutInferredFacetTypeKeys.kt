package io.qpointz.mill.data.metadata

/**
 * Facet-type URNs emitted by [io.qpointz.mill.data.metadata.source.LogicalLayoutMetadataSource]
 * (physical layout inference). Align with `metadata/platform-bootstrap.yaml`.
 */
object LayoutInferredFacetTypeKeys {
    const val SCHEMA: String = "urn:mill/metadata/facet-type:schema"
    const val TABLE: String = "urn:mill/metadata/facet-type:table"
    const val COLUMN: String = "urn:mill/metadata/facet-type:column"
}
