package io.qpointz.mill.source.descriptor

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize

/**
 * Strategy for resolving table name collisions across readers.
 */
enum class ConflictStrategy {
    /** Reject: fail with an error if two readers produce the same table name. */
    REJECT,
    /** Union: merge files from all readers into one table. */
    UNION;

    companion object {
        fun fromString(value: String): ConflictStrategy =
            when (value.lowercase()) {
                "reject" -> REJECT
                "union" -> UNION
                else -> throw IllegalArgumentException(
                    "Unknown conflict strategy: '$value'. Expected 'reject' or 'union'."
                )
            }
    }
}

/**
 * Conflict resolution configuration for multi-reader sources.
 *
 * Supports two YAML forms:
 *
 * **String shorthand** — applies to all tables:
 * ```yaml
 * conflicts: reject
 * ```
 *
 * **Map form** — default + per-table rules:
 * ```yaml
 * conflicts:
 *   default: reject
 *   orders: union
 *   customers: reject
 * ```
 *
 * @property default the default strategy for tables without an explicit rule
 * @property rules   per-table-name overrides (table name -> strategy)
 */
@JsonDeserialize(using = ConflictResolutionDeserializer::class)
data class ConflictResolution(
    val default: ConflictStrategy = ConflictStrategy.REJECT,
    val rules: Map<String, ConflictStrategy> = emptyMap()
) {

    /**
     * Returns the strategy for the given [tableName].
     * Explicit per-table rules take precedence over the default.
     */
    fun strategyFor(tableName: String): ConflictStrategy =
        rules[tableName] ?: default

    /**
     * Returns `true` if there is an explicit rule for [tableName].
     */
    fun hasExplicitRule(tableName: String): Boolean =
        rules.containsKey(tableName)

    companion object {
        /** Default: reject all collisions. */
        val DEFAULT = ConflictResolution()
    }
}

/**
 * Custom deserializer that handles both string shorthand and map form.
 */
class ConflictResolutionDeserializer : ValueDeserializer<ConflictResolution>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConflictResolution {
        val node = ctxt.readTree(p) as JsonNode

        if (node.isTextual) {
            return ConflictResolution(
                default = ConflictStrategy.fromString(node.asText())
            )
        }

        if (node.isObject) {
            val defaultStrategy = if (node.has("default")) {
                ConflictStrategy.fromString(node.get("default").asText())
            } else {
                ConflictStrategy.REJECT
            }

            val rules = mutableMapOf<String, ConflictStrategy>()
            for (key in node.propertyNames()) {
                if (key != "default") {
                    rules[key] = ConflictStrategy.fromString(node.get(key).asText())
                }
            }

            return ConflictResolution(default = defaultStrategy, rules = rules)
        }

        throw ctxt.weirdStringException(
            node.toString(),
            ConflictResolution::class.java,
            "Expected a string (e.g. 'reject') or an object with 'default' and table-specific rules"
        )
    }
}
