package io.qpointz.mill.ai.test.scenario.v3

import java.util.UUID

/**
 * Produces deterministic maps for baseline comparison by scrubbing volatile fields.
 */
object RecordNormalizer {

    private val UUID_PATTERN = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    private val OMIT_KEYS = setOf(
        "recordedAt",
        "gitCommit",
        "inputTokens",
        "outputTokens",
        "totalTokens",
        "eventId",
        "chatId",
        "itemId",
        "artifactId",
        "runId",
        "turnId",
        "timestamp",
        "createdAt",
        "durationMs",
    )

    /**
     * Normalizes a regression record map for stable baseline diff.
     *
     * @param record Raw record as a nested map (pre-JSON).
     */
    fun normalize(record: Map<String, Any?>): Map<String, Any?> =
        normalizeValue(record) as Map<String, Any?>

    /**
     * Normalizes a JSON string record.
     *
     * @param json Raw record JSON.
     */
    fun normalizeJson(json: String): String {
        @Suppress("UNCHECKED_CAST")
        val map = ScenarioPackLoader.jsonMapper.readValue(json, Map::class.java) as Map<String, Any?>
        return ScenarioPackLoader.jsonMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(normalize(map))
    }

    @Suppress("UNCHECKED_CAST")
    private fun normalizeValue(value: Any?): Any? = when (value) {
        null -> null
        is Map<*, *> -> {
            val normalized = linkedMapOf<String, Any?>()
            value.forEach { (k, v) ->
                val key = k.toString()
                if (key in OMIT_KEYS) return@forEach
                if (key == "runMeta" && v is Map<*, *>) {
                    normalized[key] = (v as Map<String, Any?>).filterKeys { it != "gitCommit" }
                        .mapValues { (_, mv) -> normalizeValue(mv) }
                } else {
                    normalized[key] = normalizeValue(v)
                }
            }
            normalized
        }
        is List<*> -> value.map { normalizeValue(it) }
        is String -> normalizeString(value)
        else -> value
    }

    private fun normalizeString(text: String): String =
        when {
            UUID_PATTERN.matches(text) -> "<uuid>"
            runCatching { UUID.fromString(text) }.isSuccess -> "<uuid>"
            else -> text
        }
}
