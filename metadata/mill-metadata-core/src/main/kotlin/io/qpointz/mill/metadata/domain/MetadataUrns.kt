package io.qpointz.mill.metadata.domain

import io.qpointz.mill.UrnSlug
import kotlin.jvm.JvmStatic

/**
 * Central registry of Mill metadata URN constants and normalisation helpers.
 *
 * All Mill object identifiers follow: `urn:mill/<domain>/<type>:<local-id>`.
 * This object provides:
 * - Prefix constants used by [UrnSlug] for prefixed-slug encoding on path variables.
 * - Per-type constants for platform facet types and the global scope (entity-type URNs live in the data schema module).
 * - Conversion functions to normalise legacy short keys and path variables to full URNs.
 *
 * @see UrnSlug
 */
object MetadataUrns {

    // ── URN namespace prefixes ────────────────────────────────────────

    /** URN prefix for facet-type identifiers, e.g. `urn:mill/metadata/facet-type:descriptive`. */
    const val FACET_TYPE_PREFIX = "urn:mill/metadata/facet-type:"

    /** URN prefix for scope identifiers, e.g. `urn:mill/metadata/scope:global`. */
    const val SCOPE_PREFIX = "urn:mill/metadata/scope:"

    /** URN prefix for persisted metadata entity ids, e.g. `urn:mill/metadata/entity:schema.table.col`. */
    const val ENTITY_PREFIX = "urn:mill/metadata/entity:"

    /**
     * URN prefix for entity-type identifiers in facet manifests (`applicableTo`). Full vocabulary
     * URNs are platform strings (see `platform-facet-types.json`); named constants live in
     * `mill-data-schema-core` as [io.qpointz.mill.data.schema.SchemaEntityTypeUrns].
     */
    const val ENTITY_TYPE_PREFIX = "urn:mill/metadata/entity-type:"

    // ── Platform facet-type URNs ──────────────────────────────────────

    /** Full URN for the `descriptive` platform facet type. */
    const val FACET_TYPE_DESCRIPTIVE = "urn:mill/metadata/facet-type:descriptive"

    /** Full URN for the `structural` platform facet type. */
    const val FACET_TYPE_STRUCTURAL = "urn:mill/metadata/facet-type:structural"

    /** Full URN for the `relation` platform facet type. */
    const val FACET_TYPE_RELATION = "urn:mill/metadata/facet-type:relation"

    /** Full URN for the `concept` platform facet type. */
    const val FACET_TYPE_CONCEPT = "urn:mill/metadata/facet-type:concept"

    /** Full URN for the `value-mapping` platform facet type. */
    const val FACET_TYPE_VALUE_MAPPING = "urn:mill/metadata/facet-type:value-mapping"

    // ── Scope URNs ────────────────────────────────────────────────────

    /** Full URN for the global scope. */
    const val SCOPE_GLOBAL = "urn:mill/metadata/scope:global"

    /**
     * Returns the full URN for a user-scoped scope.
     *
     * @param userId the user identifier, e.g. `"alice"`
     * @return full URN, e.g. `"urn:mill/metadata/scope:user:alice"`
     */
    fun scopeUser(userId: String) = "urn:mill/metadata/scope:user:$userId"

    /**
     * Returns the full URN for a team-scoped scope.
     *
     * @param teamName the team name, e.g. `"eng"`
     * @return full URN, e.g. `"urn:mill/metadata/scope:team:eng"`
     */
    fun scopeTeam(teamName: String) = "urn:mill/metadata/scope:team:$teamName"

    /**
     * Returns the full URN for a role-scoped scope.
     *
     * @param roleName the role name, e.g. `"admin"`
     * @return full URN, e.g. `"urn:mill/metadata/scope:role:admin"`
     */
    fun scopeRole(roleName: String) = "urn:mill/metadata/scope:role:$roleName"

    // ── Normalisation helpers ─────────────────────────────────────────

    /**
     * Normalises a legacy short facet-type key to its full URN.
     *
     * Returns the input unchanged if it is already a URN (starts with `"urn:"`).
     *
     * | Input | Output |
     * |---|---|
     * | `"descriptive"` | [FACET_TYPE_DESCRIPTIVE] |
     * | `"structural"` | [FACET_TYPE_STRUCTURAL] |
     * | `"relation"` | [FACET_TYPE_RELATION] |
     * | `"concept"` | [FACET_TYPE_CONCEPT] |
     * | `"value-mapping"` | [FACET_TYPE_VALUE_MAPPING] |
     * | any URN | unchanged |
     * | unknown short key | returned as-is |
     *
     * @param key the facet type key to normalise
     * @return the corresponding full URN, or [key] unchanged if not a known short key
     */
    fun normaliseFacetTypeKey(key: String): String = when (key) {
        "descriptive"   -> FACET_TYPE_DESCRIPTIVE
        "structural"    -> FACET_TYPE_STRUCTURAL
        "relation"      -> FACET_TYPE_RELATION
        "concept"       -> FACET_TYPE_CONCEPT
        "value-mapping" -> FACET_TYPE_VALUE_MAPPING
        else            -> key
    }

    /**
     * Normalises a legacy short scope key to its full URN form.
     *
     * | Input | Output |
     * |---|---|
     * | `"global"` | [SCOPE_GLOBAL] |
     * | `"user:alice"` | `"urn:mill/metadata/scope:user:alice"` |
     * | `"team:eng"` | `"urn:mill/metadata/scope:team:eng"` |
     * | `"role:admin"` | `"urn:mill/metadata/scope:role:admin"` |
     * | any URN | unchanged |
     *
     * @param key the scope key to normalise
     * @return the corresponding full URN, or [key] unchanged for unknown inputs
     */
    fun normaliseScopeKey(key: String): String = when {
        key == "global"            -> SCOPE_GLOBAL
        key.startsWith("user:")    -> "urn:mill/metadata/scope:$key"
        key.startsWith("team:")    -> "urn:mill/metadata/scope:$key"
        key.startsWith("role:")    -> "urn:mill/metadata/scope:$key"
        key.startsWith("urn:")     -> key
        else                       -> key
    }

    /**
     * Encodes a URN to a URL-safe slug for use in path segments.
     *
     * Delegates to [UrnSlug.encode] from `core/mill-core`.
     *
     * @param urn the full URN to encode
     * @return URL-safe slug
     */
    fun toSlug(urn: String): String = UrnSlug.encode(urn)

    /**
     * Normalises a facet-type path variable — accepts a prefixed slug, a legacy short key,
     * or a full URN.
     *
     * Controllers call this on every `{typeKey}` path variable before calling service methods.
     *
     * | Input example | Normalised output |
     * |---|---|
     * | `"descriptive"` | `"urn:mill/metadata/facet-type:descriptive"` |
     * | `"governance"` | `"urn:mill/metadata/facet-type:governance"` |
     * | `"urn:mill/metadata/facet-type:descriptive"` | unchanged |
     *
     * @param value the raw path variable value
     * @return normalised full URN in the facet-type namespace
     */
    @JvmStatic
    fun normaliseFacetTypePath(value: String): String =
        UrnSlug.normalise(value, FACET_TYPE_PREFIX, ::normaliseFacetTypeKey)

    /**
     * Normalises a scope path variable — accepts a prefixed slug or a full URN.
     *
     * Only URNs within the [SCOPE_PREFIX] namespace are accepted.
     *
     * | Input example | Normalised output |
     * |---|---|
     * | `"global"` | `"urn:mill/metadata/scope:global"` |
     * | `"user:alice"` | `"urn:mill/metadata/scope:user:alice"` |
     * | `"team:eng"` | `"urn:mill/metadata/scope:team:eng"` |
     *
     * @param value the raw path variable value
     * @return normalised full URN in the scope namespace
     * @throws IllegalArgumentException if the value is a URN outside the scope namespace
     */
    fun normaliseScopePath(value: String): String =
        UrnSlug.normalise(value, SCOPE_PREFIX)
}
