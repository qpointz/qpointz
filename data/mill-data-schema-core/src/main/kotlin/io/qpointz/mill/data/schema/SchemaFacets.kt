package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.FacetPayloadUtils
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.core.ConceptFacet
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.data.schema.facet.RelationFacet
import io.qpointz.mill.data.schema.facet.StructuralFacet
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet
import io.qpointz.mill.metadata.domain.facet.FacetInstance
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.service.FacetCatalog

/**
 * Facet holder backed by a map keyed on facet type.
 *
 * Typed convenience properties cover the standard platform facet types.
 * Unknown or custom facet types are accessible via [facetByType].
 *
 * @param facets typed facet payloads for schema explorer / JDBC shaping
 * @param facetsResolved unified read rows (captured + inferred) after multi-source merge (SPEC §3b)
 */
class SchemaFacets(
    facets: Set<MetadataFacet>,
    val facetsResolved: List<FacetInstance> = emptyList()
) {

    private val facetMap: Map<String, MetadataFacet> = facets.associateBy { it.facetType }

    /** Human-readable metadata: display name, description, tags, business domain, etc. */
    val descriptive: DescriptiveFacet?   get() = facetByType("descriptive")

    /** Physical schema binding: physical name, type, nullability, primary/foreign key flags, etc. */
    val structural: StructuralFacet?     get() = facetByType("structural")

    /** Cross-entity relationships declared on this object. */
    val relation: RelationFacet?         get() = facetByType("relation")

    /** Business concept definitions associated with this object. */
    val concept: ConceptFacet?           get() = facetByType("concept")

    /** Attribute value normalization mappings used for NL2SQL resolution. */
    val valueMapping: ValueMappingFacet? get() = facetByType("value-mapping")

    /** Set of facet type keys present in this holder. */
    val facetTypes: Set<String> get() = facetMap.keys

    /** True when no typed facets and no resolved read rows are held. */
    val isEmpty: Boolean get() = facetMap.isEmpty() && facetsResolved.isEmpty()

    /**
     * Returns the facet for the given [type] key cast to [T], or null if absent or the cast fails.
     * Use this for custom or future facet types not covered by the typed convenience properties.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : MetadataFacet> facetByType(type: String): T? = facetMap[type] as? T

    companion object {
        /** Shared empty instance returned for physical objects that have no matched metadata. */
        val EMPTY = SchemaFacets(emptySet(), emptyList())

        /**
         * Builds typed [MetadataFacet] accessors plus [facetsResolved] from merged read rows.
         *
         * @param resolved output of [io.qpointz.mill.metadata.service.FacetInstanceReadMerge.merge]
         * @param catalog facet type cardinality (MULTIPLE merges relation payloads)
         */
        fun fromResolved(
            resolved: List<FacetInstance>,
            catalog: FacetCatalog
        ): SchemaFacets {
            if (resolved.isEmpty()) return EMPTY
            val facets = mutableSetOf<MetadataFacet>()
            val byType = resolved.groupBy { MetadataEntityUrn.canonicalize(it.facetTypeKey) }
            for ((canonType, rows) in byType.toSortedMap()) {
                val card = catalog.findDefinition(canonType)?.targetCardinality
                    ?: FacetTargetCardinality.SINGLE
                when (shortKey(canonType)) {
                    "descriptive" -> convertOne(rows, card, DescriptiveFacet::class.java, facets)
                    "structural" -> convertOne(rows, card, StructuralFacet::class.java, facets)
                    "relation" -> convertRelation(rows, card, facets)
                    "concept" -> convertOne(rows, card, ConceptFacet::class.java, facets)
                    "value-mapping" -> convertOne(rows, card, ValueMappingFacet::class.java, facets)
                    else -> Unit
                }
            }
            return SchemaFacets(facets, resolved)
        }

        private fun shortKey(canonType: String): String = when (canonType) {
            MetadataUrns.FACET_TYPE_DESCRIPTIVE -> "descriptive"
            MetadataUrns.FACET_TYPE_STRUCTURAL -> "structural"
            MetadataUrns.FACET_TYPE_RELATION -> "relation"
            MetadataUrns.FACET_TYPE_CONCEPT -> "concept"
            MetadataUrns.FACET_TYPE_VALUE_MAPPING -> "value-mapping"
            else -> canonType.removePrefix(MetadataUrns.FACET_TYPE_PREFIX)
        }

        private fun <T : MetadataFacet> convertOne(
            rows: List<FacetInstance>,
            card: FacetTargetCardinality,
            clazz: Class<T>,
            out: MutableSet<MetadataFacet>
        ) {
            if (rows.isEmpty()) return
            val row = if (card == FacetTargetCardinality.MULTIPLE) rows.first() else rows.first()
            FacetPayloadUtils.convert(row.payload, clazz).ifPresent { out.add(it) }
        }

        private fun convertRelation(
            rows: List<FacetInstance>,
            card: FacetTargetCardinality,
            out: MutableSet<MetadataFacet>
        ) {
            if (rows.isEmpty()) return
            if (card == FacetTargetCardinality.MULTIPLE && rows.size > 1) {
                val merged = LinkedHashMap<String, Any?>()
                val allRelations = rows.flatMap { row ->
                    (row.payload["relations"] as? Iterable<*>)?.toList().orEmpty()
                }
                if (allRelations.isNotEmpty()) {
                    merged["relations"] = allRelations
                    FacetPayloadUtils.convert(merged, RelationFacet::class.java).ifPresent { out.add(it) }
                } else {
                    FacetPayloadUtils.convert(rows.first().payload, RelationFacet::class.java).ifPresent { out.add(it) }
                }
            } else {
                FacetPayloadUtils.convert(rows.first().payload, RelationFacet::class.java).ifPresent { out.add(it) }
            }
        }
    }
}
