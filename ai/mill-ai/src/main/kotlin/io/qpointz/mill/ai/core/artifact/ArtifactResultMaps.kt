package io.qpointz.mill.ai.core.artifact

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

private val resultMapper: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .build()

/**
 * Coerces structured tool results (maps or Kotlin data classes) to a string-keyed map.
 *
 * @param result Tool handler payload.
 * @return Normalized map for routing and emission predicates.
 */
fun structuredResultMap(result: Any?): Map<String, Any?>? =
    when (result) {
        null -> null
        is Map<*, *> -> result.entries.associate { it.key.toString() to it.value }
        else -> runCatching {
            resultMapper.convertValue(result, object : TypeReference<Map<String, Any?>>() {})
        }.getOrNull()
    }
