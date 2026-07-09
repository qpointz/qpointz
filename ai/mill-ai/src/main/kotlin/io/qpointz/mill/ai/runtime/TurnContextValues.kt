package io.qpointz.mill.ai.runtime

/**
 * Ephemeral per-turn host metadata from `POST .../messages` `context.values`.
 *
 * Not persisted on durable turns; consumed for prompt assembly and tooling for one agent run.
 *
 * @property values host-supplied context map (unknown keys tolerated)
 * @property version optional client schema version; omitted unless migration requires it
 */
data class TurnContextValues(
    val values: Map<String, Any?> = emptyMap(),
    val version: Int? = null,
) {
    fun stringValue(key: String): String? =
        values[key]?.let { value ->
            when (value) {
                is String -> value.takeIf { it.isNotBlank() }
                else -> value.toString().takeIf { it.isNotBlank() }
            }
        }

    companion object {
        /** Builds turn context from a wire map; null/empty maps become absent context. */
        fun fromWire(values: Map<String, Any?>?, version: Int? = null): TurnContextValues? {
            if (values.isNullOrEmpty()) return null
            return TurnContextValues(values = values, version = version)
        }
    }
}
