package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.RelationCardinality

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

    /**
     * Normalizes one facet row from `relation`, `relation-source`, or `relation-target` into
     * canonical relation maps.
     *
     * @param facetTypeKey short or canonical facet type key (`relation`, `relation-source`, …)
     * @param payload raw facet payload
     * @param ownerSchema physical schema of the metadata entity hosting the facet
     * @param ownerTable physical table of the hosting entity, or null for schema-level facets
     * @return zero or more relation entry maps
     */
    fun normalizeFacetRow(
        facetTypeKey: String,
        payload: Map<String, Any?>,
        ownerSchema: String?,
        ownerTable: String?,
    ): List<Map<String, Any?>> {
        val shortKey = facetTypeShortKey(facetTypeKey)
        return when (shortKey) {
            "relation-source" -> {
                if (ownerSchema == null || ownerTable == null) {
                    emptyList()
                } else {
                    relationSourceToRelation(payload, ownerSchema, ownerTable)?.let { listOf(it) }
                        ?: emptyList()
                }
            }
            "relation-target" -> {
                if (ownerSchema == null || ownerTable == null) {
                    emptyList()
                } else {
                    relationTargetToRelation(payload, ownerSchema, ownerTable)?.let { listOf(it) }
                        ?: emptyList()
                }
            }
            else -> {
                val norm = normalizeToRelationPayload(payload)
                @Suppress("UNCHECKED_CAST")
                (norm["relations"] as? List<Map<String, Any?>>) ?: emptyList()
            }
        }
    }

    /**
     * Maps a `relation-source` payload to a canonical relation with the owner table as source.
     *
     * @param payload relation-source facet payload
     * @param ownerSchema schema of the owning table entity
     * @param ownerTable table name of the owning table entity
     */
    fun relationSourceToRelation(
        payload: Map<String, Any?>,
        ownerSchema: String,
        ownerTable: String,
    ): Map<String, Any?>? {
        val target = payload["target"] as? Map<*, *> ?: return null
        val targetSchema = target["schema"]?.toString() ?: return null
        val targetTable = target["table"]?.toString() ?: return null
        val sourceColumns = columnList(payload["sourceColumns"])
        val targetColumns = columnList(target["columns"])
        if (sourceColumns.isEmpty() || targetColumns.isEmpty()) {
            return null
        }
        val out = LinkedHashMap<String, Any?>()
        out["sourceTable"] = mapOf("schema" to ownerSchema, "table" to ownerTable)
        out["sourceAttributes"] = sourceColumns
        out["targetTable"] = mapOf("schema" to targetSchema, "table" to targetTable)
        out["targetAttributes"] = targetColumns
        copyCommonFields(payload, out)
        if (out["name"] == null) {
            out["name"] = defaultSourceNavigationName(sourceColumns, targetTable, targetColumns)
        }
        return out
    }

    /**
     * Maps a `relation-target` payload to a canonical relation from the owner table's perspective
     * (owner is source, referenced table is target).
     *
     * @param payload relation-target facet payload
     * @param ownerSchema schema of the owning table entity
     * @param ownerTable table name of the owning table entity
     */
    fun relationTargetToRelation(
        payload: Map<String, Any?>,
        ownerSchema: String,
        ownerTable: String,
    ): Map<String, Any?>? {
        val source = payload["source"] as? Map<*, *> ?: return null
        val sourceSchema = source["schema"]?.toString() ?: return null
        val sourceTable = source["table"]?.toString() ?: return null
        val targetColumns = columnList(payload["targetColumns"])
        val sourceColumns = columnList(source["columns"])
        if (targetColumns.isEmpty() || sourceColumns.isEmpty()) {
            return null
        }
        val out = LinkedHashMap<String, Any?>()
        out["sourceTable"] = mapOf("schema" to ownerSchema, "table" to ownerTable)
        out["sourceAttributes"] = targetColumns
        out["targetTable"] = mapOf("schema" to sourceSchema, "table" to sourceTable)
        out["targetAttributes"] = sourceColumns
        copyCommonFields(payload, out)
        payload["cardinality"]?.let { out["cardinality"] = invertCardinality(it.toString()) }
        if (out["name"] == null) {
            out["name"] = defaultTargetNavigationName(targetColumns, sourceTable)
        }
        return out
    }

    /**
     * Inverts cardinality from the referenced table's perspective to the owner table's perspective.
     *
     * @param cardinality cardinality string from metadata
     */
    fun invertCardinality(cardinality: String): String =
        when (cardinality) {
            RelationCardinality.ONE_TO_MANY.name -> RelationCardinality.MANY_TO_ONE.name
            RelationCardinality.MANY_TO_ONE.name -> RelationCardinality.ONE_TO_MANY.name
            else -> cardinality
        }

    /**
     * Default OData navigation name for an outbound `relation-source` edge.
     *
     * @param sourceColumns FK columns on the source table
     * @param targetTable target table name
     * @param targetColumns join columns on the target table
     */
    fun defaultSourceNavigationName(
        sourceColumns: List<String>,
        targetTable: String,
        targetColumns: List<String>,
    ): String {
        val targetAttr = targetColumns.singleOrNull()
        if (targetAttr != null) {
            return "${targetAttr}_$targetTable"
        }
        val sourceCol = sourceColumns.singleOrNull()
        return if (sourceCol != null) {
            "${sourceCol}_$targetTable"
        } else {
            targetTable
        }
    }

    /**
     * Default OData navigation name for a `relation-target` edge after flipping to owner-as-source.
     *
     * @param targetColumns FK columns on the owner (target) table
     * @param sourceTable referenced source table name
     */
    fun defaultTargetNavigationName(targetColumns: List<String>, sourceTable: String): String {
        val fk = targetColumns.singleOrNull()
        return if (fk != null) {
            "${fk}_$sourceTable"
        } else {
            sourceTable
        }
    }

    private fun facetTypeShortKey(facetTypeKey: String): String {
        val canon = io.qpointz.mill.metadata.domain.MetadataUrns.normaliseFacetTypeKey(facetTypeKey)
        return when (canon) {
            io.qpointz.mill.metadata.domain.MetadataUrns.FACET_TYPE_RELATION -> "relation"
            else -> canon.removePrefix(io.qpointz.mill.metadata.domain.MetadataUrns.FACET_TYPE_PREFIX)
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
        copyCommonFields(parent, out)
        return out
    }

    private fun copyCommonFields(from: Map<String, Any?>, to: MutableMap<String, Any?>) {
        from["name"]?.let { to["name"] = it }
        from["description"]?.let { to["description"] = it }
        if (!to.containsKey("cardinality")) {
            from["cardinality"]?.let { to["cardinality"] = it }
        }
        from["joinSql"]?.let { to["joinSql"] = it }
        from["type"]?.let { to["type"] = it }
        from["businessMeaning"]?.let { to["businessMeaning"] = it }
    }

    private fun columnList(columns: Any?): List<String> =
        (columns as? Iterable<*>)?.map { it.toString() } ?: emptyList()
}
