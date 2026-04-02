package io.qpointz.mill.metadata.domain

/**
 * Structural representation of a parsed Mill URN.
 *
 * ## Grammar
 *
 * ```
 * urn:<namespace>/<group>/<kind>:<id>
 * ```
 *
 * | Segment | Example | Meaning |
 * |---------|---------|---------|
 * | `namespace` | `mill` | URN authority; always `mill` for platform URNs |
 * | `group` | `data`, `metadata` | Logical subsystem owning the identifier |
 * | `kind` | `table`, `facet-type`, `scope` | Entity class within the group |
 * | `id` | `sales.orders`, `user:alice` | Local identifier; may contain dots or colons |
 *
 * ## Opaque contract
 *
 * `mill-metadata-core` treats entity URNs as **opaque strings** — it never interprets
 * `group` or `kind`. This class is a **structural facility** only: it provides grammar
 * parsing so downstream modules (e.g. `mill-data-metadata`) can attach semantic meaning to
 * specific `group`/`kind` combinations without duplicating parsing logic.
 *
 * ## Canonical form
 *
 * Instances produced by [parse] always carry **lowercase** components. Direct constructor
 * use bypasses normalisation; prefer [parse] or the normalised builder.
 *
 * @property namespace URN namespace identifier, e.g. `"mill"`
 * @property group Logical subsystem group, e.g. `"data"` or `"metadata"`
 * @property kind Entity class within the group, e.g. `"table"` or `"facet-type"`
 * @property id Local identifier, e.g. `"sales.orders"` or `"user:alice"`
 */
data class MillUrn(
    val namespace: String,
    val group: String,
    val kind: String,
    val id: String,
) {

    /**
     * Reconstructs the URN string from the structural components.
     *
     * The result reflects whatever case the components carry — instances from [parse] produce
     * a lowercase (canonical) URN.
     *
     * @return `"urn:<namespace>/<group>/<kind>:<id>"`
     */
    val raw: String
        get() = "urn:$namespace/$group/$kind:$id"

    companion object {

        /**
         * Pattern: `urn:<ns>/<group>/<kind>:<id>`
         *
         * - Namespace and group allow any character except `/`.
         * - Kind allows any character except `:` (hyphens such as `facet-type` are valid).
         * - Id allows any character including `:` and `.` (e.g. `user:alice`, `schema.table.col`).
         */
        private val PATTERN = Regex("""^urn:([^/]+)/([^/]+)/([^:]+):(.+)$""")

        /**
         * Parses a URN string into its structural components.
         *
         * Input is trimmed and lowercased before parsing. Returns `null` for any string
         * that does not conform to the `urn:<ns>/<group>/<kind>:<id>` grammar, including
         * blank strings or URNs with an empty id.
         *
         * @param raw the URN string to parse
         * @return parsed [MillUrn] with canonical (lowercase) components, or `null`
         */
        fun parse(raw: String): MillUrn? {
            val c = raw.trim().lowercase()
            val m = PATTERN.matchEntire(c) ?: return null
            return MillUrn(
                namespace = m.groupValues[1],
                group     = m.groupValues[2],
                kind      = m.groupValues[3],
                id        = m.groupValues[4],
            )
        }

        /**
         * Parses a URN string and throws if it does not conform to the grammar.
         *
         * @param raw the URN string to parse
         * @return parsed [MillUrn]
         * @throws IllegalArgumentException if [raw] is not a valid `urn:<ns>/<group>/<kind>:<id>`
         */
        fun parseOrThrow(raw: String): MillUrn =
            parse(raw) ?: throw IllegalArgumentException("Not a valid Mill URN: $raw")
    }
}
