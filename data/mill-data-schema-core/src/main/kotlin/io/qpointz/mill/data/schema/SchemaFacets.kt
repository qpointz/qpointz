package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.core.ConceptFacet
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.metadata.domain.core.StructuralFacet
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet

/**
 * Facet holder backed by a map keyed on facet type.
 *
 * Typed convenience properties cover the standard platform facet types.
 * Unknown or custom facet types are accessible via [facetByType].
 */
class SchemaFacets(facets: Set<MetadataFacet>) {

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

    /** True when no facets are held; typically the case for physical objects with no matched metadata. */
    val isEmpty: Boolean get() = facetMap.isEmpty()

    /**
     * Returns the facet for the given [type] key cast to [T], or null if absent or the cast fails.
     * Use this for custom or future facet types not covered by the typed convenience properties.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : MetadataFacet> facetByType(type: String): T? = facetMap[type] as? T

    companion object {
        /** Shared empty instance returned for physical objects that have no matched metadata. */
        val EMPTY = SchemaFacets(emptySet())
    }
}
