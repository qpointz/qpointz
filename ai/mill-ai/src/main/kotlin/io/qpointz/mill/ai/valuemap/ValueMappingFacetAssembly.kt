package io.qpointz.mill.ai.valuemap

import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import java.time.Duration

/**
 * Parses WI-181 facet assignments and builds a [ValueSource] for indexing (WI-182 § Production metadata retrieval).
 */
object ValueMappingFacetAssembly {

    /**
     * Primary `ai-column-value-mapping` controls (subset of payload).
     */
    data class PrimaryFacetPayload(
        val enabled: Boolean,
        val refreshAtStartUp: Boolean,
        val refreshInterval: Duration?,
        val context: String,
        val indexNull: Boolean,
        val nullContent: String,
    )

    /**
     * Reads the primary facet from loaded assignments, if present.
     */
    fun parsePrimaryFromFacets(facets: List<FacetAssignment>): PrimaryFacetPayload? {
        val primary = facets.find { it.facetTypeKey == ValueMappingIndexingFacetTypes.AI_COLUMN_VALUE_MAPPING }
            ?: return null
        return parsePrimaryPayload(primary.payload)
    }

    fun parsePrimaryPayload(payload: Map<String, Any?>): PrimaryFacetPayload? {
        val data = payload["data"] as? Map<*, *> ?: return null
        val enabled = data["enabled"] as? Boolean ?: return null
        val refreshAtStartUp = data["refreshAtStartUp"] as? Boolean ?: false
        val intervalStr = data["refreshInterval"] as? String
        val context = payload["context"] as? String ?: return null
        val nv = payload["nullValues"] as? Map<*, *>
        val indexNull = nv?.get("indexNull") as? Boolean ?: false
        val nullContent = nv?.get("nullContent") as? String ?: ""
        val interval = intervalStr?.let { MillDurations.parseLenient(it) }
        return PrimaryFacetPayload(
            enabled = enabled,
            refreshAtStartUp = refreshAtStartUp,
            refreshInterval = interval,
            context = context,
            indexNull = indexNull,
            nullContent = nullContent,
        )
    }

    /**
     * Builds a composite [ValueSource] from primary + static facets and DISTINCT results.
     *
     * @param distinctCellValues cell values from DISTINCT (`null` marks a NULL bucket when present in the result set)
     */
    fun buildValueSource(
        facets: List<FacetAssignment>,
        distinctCellValues: List<String?>,
    ): ValueSource? {
        val primaryAssignment = facets.find { it.facetTypeKey == ValueMappingIndexingFacetTypes.AI_COLUMN_VALUE_MAPPING }
            ?: return null
        val primary = parsePrimaryPayload(primaryAssignment.payload) ?: return null
        if (!primary.enabled) {
            return null
        }
        val children = mutableListOf<ValueSource>()
        val nonNullCells = distinctCellValues.filterNotNull()
        if (nonNullCells.isNotEmpty()) {
            children.add(DistinctColumnValueSource(primary.context, nonNullCells))
        }
        if (primary.indexNull && distinctCellValues.any { it == null }) {
            val line = primary.context + primary.nullContent
            children.add(
                object : ValueSource {
                    override fun provideEntries(): List<AttributeValueEntry> =
                        listOf(
                            AttributeValueEntry(
                                content = line,
                                metadata = mapOf("isNull" to "true", "value" to ""),
                            ),
                        )
                },
            )
        }
        val staticPairs = facets
            .filter { it.facetTypeKey == ValueMappingIndexingFacetTypes.AI_COLUMN_VALUE_MAPPING_VALUES }
            .flatMap { parseStaticRows(it.payload) }
        if (staticPairs.isNotEmpty()) {
            children.add(StaticListValueSource(staticPairs))
        }
        if (children.isEmpty()) {
            return null
        }
        return CompositeValueSource(children)
    }

    private fun parseStaticRows(payload: Map<String, Any?>): List<Pair<String, String>> {
        val values = payload["values"] as? List<*> ?: return emptyList()
        return values.mapNotNull { row ->
            val m = row as? Map<*, *> ?: return@mapNotNull null
            val c = m["content"] as? String ?: return@mapNotNull null
            val v = m["value"] as? String ?: return@mapNotNull null
            c to v
        }
    }
}
