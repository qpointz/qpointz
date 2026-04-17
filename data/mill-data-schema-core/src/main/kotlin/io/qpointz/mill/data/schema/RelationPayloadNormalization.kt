package io.qpointz.mill.data.schema

/**
 * Normalizes relation facet JSON payloads into the shape expected by
 * [io.qpointz.mill.data.schema.facet.RelationFacet] (a top-level `relations` list of
 * [io.qpointz.mill.data.schema.facet.RelationFacet.Relation] maps).
 *
 * The **platform-bootstrap** seed grammar (see `metadata/platform-bootstrap.yaml`, relation facet
 * `contentSchema`) stores each logical edge as nested `source` / `target` objects with
 * `columns` arrays. The schema layer and JDBC-facing [RelationFacet] type use `sourceTable` /
 * `targetTable` ([io.qpointz.mill.metadata.domain.core.TableLocator]) and `sourceAttributes` /
 * `targetAttributes` instead; this object performs that mapping so [SchemaFacets] can merge
 * multiple facet rows without losing bootstrap-shaped instances.
 */
object RelationPayloadNormalization {

    /**
     * Returns a payload map containing a `relations` list suitable for deserialization into
     * [io.qpointz.mill.data.schema.facet.RelationFacet].
     *
     * @param payload raw merged facet payload from metadata (may be bootstrap-shaped, canonical
     * `relations` array, or a single canonical relation entry)
     */
    fun normalizeToRelationPayload(payload: Map<String, Any?>): Map<String, Any?> {
        val relations = payload["relations"]
        if (relations is List<*>) {
            val maps = relations.mapNotNull { normalizeSingleRelation(it as? Map<String, Any?>) }
            return mapOf(
                "relations" to maps,
                "facetType" to (payload["facetType"] as? String ?: "relation"),
            )
        }
        val single = normalizeSingleRelation(payload)
        return if (single != null) {
            mapOf("relations" to listOf(single), "facetType" to "relation")
        } else {
            mapOf("relations" to emptyList<Map<String, Any?>>(), "facetType" to "relation")
        }
    }

    private fun normalizeSingleRelation(m: Map<String, Any?>?): Map<String, Any?>? {
        if (m == null || m.isEmpty()) return null
        val src = m["source"] as? Map<*, *>
        val tgt = m["target"] as? Map<*, *>
        if (src != null && tgt != null) {
            return bootstrapNestedToRelation(src, tgt, m)
        }
        if (m.containsKey("sourceTable") || m.containsKey("targetTable")) {
            @Suppress("UNCHECKED_CAST")
            return m as Map<String, Any?>
        }
        return null
    }

    private fun bootstrapNestedToRelation(
        source: Map<*, *>,
        target: Map<*, *>,
        parent: Map<String, Any?>,
    ): Map<String, Any?> {
        val out = LinkedHashMap<String, Any?>()
        out["sourceTable"] = mapOf(
            "schema" to source["schema"].toString(),
            "table" to source["table"].toString(),
        )
        out["sourceAttributes"] = columnList(source["columns"])
        out["targetTable"] = mapOf(
            "schema" to target["schema"].toString(),
            "table" to target["table"].toString(),
        )
        out["targetAttributes"] = columnList(target["columns"])
        parent["name"]?.let { out["name"] = it }
        parent["description"]?.let { out["description"] = it }
        parent["cardinality"]?.let { out["cardinality"] = it }
        parent["joinSql"]?.let { out["joinSql"] = it }
        parent["type"]?.let { out["type"] = it }
        parent["businessMeaning"]?.let { out["businessMeaning"] = it }
        return out
    }

    private fun columnList(columns: Any?): List<String> =
        (columns as? Iterable<*>)?.map { it.toString() } ?: emptyList()
}
