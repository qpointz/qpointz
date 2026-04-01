package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest

/**
 * In-code facet-type manifests for **structural**, **relation**, and **value-mapping** (tests, codegen).
 *
 * **Production seeding** loads the same shapes from `metadata/platform-bootstrap.yaml` via
 * `mill.metadata.seed.resources` — keep this object aligned when changing the YAML.
 */
object DataOwnedFacetTypeManifests {

    private fun objectPayload(title: String, description: String): FacetPayloadSchema =
        FacetPayloadSchema(
            type = FacetSchemaType.OBJECT,
            title = title,
            description = description,
            fields = emptyList(),
            required = emptyList()
        )

    /**
     * @return manifests for structural, relation, and value-mapping facet types (URN keys)
     */
    fun manifests(): List<FacetTypeManifest> {
        // Physical/table facets only; MODEL (SPEC §3f) is logical and not structural/relation/value-mapping here.
        val tableAndAttribute = listOf(SchemaEntityKinds.TABLE, SchemaEntityKinds.ATTRIBUTE)
        val attributeOnly = listOf(SchemaEntityKinds.ATTRIBUTE)
        return listOf(
            FacetTypeManifest(
                typeKey = MetadataUrns.FACET_TYPE_STRUCTURAL,
                title = "Structural",
                description = "Physical table and column attributes used by schema exploration.",
                targetCardinality = FacetTargetCardinality.SINGLE,
                applicableTo = tableAndAttribute,
                payload = objectPayload(
                    "Structural payload",
                    "Arbitrary JSON object for physical name, JDBC type, nullability, and related fields."
                )
            ),
            FacetTypeManifest(
                typeKey = MetadataUrns.FACET_TYPE_RELATION,
                title = "Relation",
                description = "Declared relationships between tables for join planning.",
                targetCardinality = FacetTargetCardinality.MULTIPLE,
                applicableTo = tableAndAttribute,
                payload = objectPayload(
                    "Relation payload",
                    "JSON object describing named relations and source/target locators."
                )
            ),
            FacetTypeManifest(
                typeKey = MetadataUrns.FACET_TYPE_VALUE_MAPPING,
                title = "Value mapping",
                description = "Maps user vocabulary to database values for NL-to-SQL and search.",
                targetCardinality = FacetTargetCardinality.SINGLE,
                applicableTo = attributeOnly,
                payload = objectPayload(
                    "Value mapping payload",
                    "JSON object for static mappings, dynamic SQL sources, and optional context."
                )
            )
        )
    }
}
