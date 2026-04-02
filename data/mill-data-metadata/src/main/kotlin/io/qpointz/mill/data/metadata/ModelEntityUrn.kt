package io.qpointz.mill.data.metadata

import io.qpointz.mill.metadata.domain.MillUrn
import java.util.Locale

/**
 * Schema-bound interpretation of typed Mill model entity URNs on top of [MillUrn].
 *
 * ## Layered design
 *
 * [MillUrn] (`mill-metadata-core`) provides **structural** parsing of any
 * `urn:<ns>/<group>/<kind>:<id>` URN without attaching semantic meaning.
 * This object is the **semantic layer**: it knows that `group = "model"` combined with
 * specific `kind` values maps to entity types in the Mill logical model — covering
 * relational catalog entities (schema, table, attribute), the logical model root, and
 * taxonomy concepts.
 *
 * ```
 * urn:mill/model/schema:<schema>
 * urn:mill/model/table:<schema>.<table>
 * urn:mill/model/attribute:<schema>.<table>.<column>
 * urn:mill/model/model:model-entity
 * urn:mill/model/concept:<id>
 * ```
 *
 * The `model` group is intentionally broader than `data`: it covers relational data
 * entities, the logical catalog root, and taxonomy concepts — anything that participates
 * in the Mill entity model.
 *
 * ## Central reuse
 *
 * All `mill-data-*` modules that need to build or decode model entity URNs must use this
 * object. [io.qpointz.mill.data.schema.MetadataEntityUrnCodec] implementations delegate
 * here; [io.qpointz.mill.data.metadata.RelationalMetadataEntityUrns] wraps it during the
 * typed-URN migration.
 *
 * @see MillUrn
 * @see CatalogPath
 */
object ModelEntityUrn {

    // ── Group ─────────────────────────────────────────────────────────────

    /** Mill URN namespace for all platform URNs. */
    private const val MILL_NS = "mill"

    /**
     * URN group for Mill model entities — covers relational catalog entities,
     * the logical model root, and taxonomy concepts.
     */
    const val GROUP = "model"

    // ── Kind constants ────────────────────────────────────────────────────

    /** URN kind for schema-level entities. */
    const val KIND_SCHEMA = "schema"

    /** URN kind for table-level entities. */
    const val KIND_TABLE = "table"

    /** URN kind for column/attribute-level entities. */
    const val KIND_ATTRIBUTE = "attribute"

    /** URN kind for the logical model root entity. */
    const val KIND_MODEL = "model"

    /** URN kind for concept/taxonomy entities. */
    const val KIND_CONCEPT = "concept"

    // ── Well-known entity id ──────────────────────────────────────────────

    /** Canonical URN for the catalog-wide logical model root entity. */
    const val MODEL_ENTITY_ID = "urn:mill/model/model:model-entity"

    // ── Builders ─────────────────────────────────────────────────────────

    /**
     * Builds the canonical typed URN for a schema-level entity.
     *
     * @param schema physical schema name
     * @return `urn:mill/model/schema:<schema>` in canonical (lowercase) form
     * @throws IllegalArgumentException if [schema] is blank
     */
    fun forSchema(schema: String): String {
        val s = schema.trim()
        require(s.isNotEmpty()) { "schema must not be blank" }
        return MillUrn(MILL_NS, GROUP, KIND_SCHEMA, s.lowercase(Locale.ROOT)).raw
    }

    /**
     * Builds the canonical typed URN for a table-level entity.
     *
     * @param schema physical schema name
     * @param table physical table name
     * @return `urn:mill/model/table:<schema>.<table>` in canonical form
     * @throws IllegalArgumentException if [schema] or [table] is blank
     */
    fun forTable(schema: String, table: String): String {
        val s = schema.trim()
        val t = table.trim()
        require(s.isNotEmpty() && t.isNotEmpty()) { "schema and table must not be blank" }
        return MillUrn(MILL_NS, GROUP, KIND_TABLE, "${s.lowercase(Locale.ROOT)}.${t.lowercase(Locale.ROOT)}").raw
    }

    /**
     * Builds the canonical typed URN for an attribute/column-level entity.
     *
     * Dots within [column] are preserved — they appear after the second dot separator in
     * the id and are decoded correctly by [parseCatalogPath].
     *
     * @param schema physical schema name
     * @param table physical table name
     * @param column physical column name
     * @return `urn:mill/model/attribute:<schema>.<table>.<column>` in canonical form
     * @throws IllegalArgumentException if any argument is blank
     */
    fun forAttribute(schema: String, table: String, column: String): String {
        val s = schema.trim()
        val t = table.trim()
        val c = column.trim()
        require(s.isNotEmpty() && t.isNotEmpty() && c.isNotEmpty()) {
            "schema, table, and column must not be blank"
        }
        return MillUrn(
            MILL_NS, GROUP, KIND_ATTRIBUTE,
            "${s.lowercase(Locale.ROOT)}.${t.lowercase(Locale.ROOT)}.${c.lowercase(Locale.ROOT)}"
        ).raw
    }

    /**
     * Builds the canonical typed URN for a concept/taxonomy entity.
     *
     * @param id concept identifier (may contain colons for sub-typed concepts, e.g. `product:123`)
     * @return `urn:mill/model/concept:<id>` in canonical form
     * @throws IllegalArgumentException if [id] is blank
     */
    fun forConcept(id: String): String {
        val i = id.trim()
        require(i.isNotEmpty()) { "concept id must not be blank" }
        return MillUrn(MILL_NS, GROUP, KIND_CONCEPT, i.lowercase(Locale.ROOT)).raw
    }

    // ── Interpretation ────────────────────────────────────────────────────

    /**
     * Returns `true` if [urn] is a structurally valid Mill URN belonging to the `model` group.
     *
     * Delegates structural parsing to [MillUrn.parse]; checks `group == "model"`.
     *
     * @param urn any string
     */
    fun isModelEntityUrn(urn: String): Boolean = parseModelUrn(urn) != null

    /**
     * Extracts the kind segment from a model entity URN.
     *
     * | Input | Result |
     * |-------|--------|
     * | `urn:mill/model/schema:sales` | `"schema"` |
     * | `urn:mill/model/table:sales.orders` | `"table"` |
     * | `urn:mill/model/attribute:sales.orders.id` | `"attribute"` |
     * | `urn:mill/model/model:model-entity` | `"model"` |
     * | `urn:mill/model/concept:product` | `"concept"` |
     * | non-model or invalid URN | `null` |
     *
     * This replaces the `entity.kind` field: entity type is derivable from the URN
     * directly without a separate stored column.
     *
     * @param urn any string
     * @return kind segment, or `null`
     */
    fun kindOf(urn: String): String? = parseModelUrn(urn)?.kind

    /**
     * Parses a typed model entity URN into relational [CatalogPath] coordinates.
     *
     * Only `schema`, `table`, and `attribute` URNs produce non-null coordinates.
     * `model`, `concept`, and unrecognised kinds return `CatalogPath(null, null, null)`.
     *
     * Dots within the column segment of an `attribute` id are preserved as the column
     * coordinate (everything after the second separator dot).
     *
     * @param urn model entity URN to decode; structurally parsed via [MillUrn.parse]
     * @return decoded [CatalogPath]
     */
    fun parseCatalogPath(urn: String): CatalogPath {
        val m = parseModelUrn(urn) ?: return empty()
        return when (m.kind) {
            KIND_SCHEMA -> CatalogPath(m.id.takeIf { it.isNotEmpty() }, null, null)
            KIND_TABLE -> decodeTableId(m.id)
            KIND_ATTRIBUTE -> decodeAttributeId(m.id)
            else -> empty()
        }
    }

    // ── Kind predicates ───────────────────────────────────────────────────

    /** @return `true` if [urn] is a schema-kind model entity URN */
    fun isSchemaUrn(urn: String): Boolean = kindOf(urn) == KIND_SCHEMA

    /** @return `true` if [urn] is a table-kind model entity URN */
    fun isTableUrn(urn: String): Boolean = kindOf(urn) == KIND_TABLE

    /** @return `true` if [urn] is an attribute-kind model entity URN */
    fun isAttributeUrn(urn: String): Boolean = kindOf(urn) == KIND_ATTRIBUTE

    /** @return `true` if [urn] is a model-kind model entity URN */
    fun isModelRootUrn(urn: String): Boolean = kindOf(urn) == KIND_MODEL

    /** @return `true` if [urn] is a concept-kind model entity URN */
    fun isConceptUrn(urn: String): Boolean = kindOf(urn) == KIND_CONCEPT

    // ── Internal helpers ──────────────────────────────────────────────────

    private fun parseModelUrn(urn: String): MillUrn? =
        MillUrn.parse(urn)?.takeIf { it.group == GROUP }

    private fun decodeTableId(id: String): CatalogPath {
        val dot = id.indexOf('.')
        if (dot <= 0) return empty()
        return CatalogPath(id.substring(0, dot), id.substring(dot + 1), null)
    }

    private fun decodeAttributeId(id: String): CatalogPath {
        val dot1 = id.indexOf('.')
        if (dot1 <= 0) return empty()
        val rest = id.substring(dot1 + 1)
        val dot2 = rest.indexOf('.')
        if (dot2 <= 0) return empty()
        return CatalogPath(
            id.substring(0, dot1),
            rest.substring(0, dot2),
            rest.substring(dot2 + 1).takeIf { it.isNotEmpty() },
        )
    }

    private fun empty() = CatalogPath(null, null, null)
}
