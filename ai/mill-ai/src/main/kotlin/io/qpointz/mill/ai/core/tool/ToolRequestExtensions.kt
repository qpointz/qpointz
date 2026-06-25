package io.qpointz.mill.ai.core.tool

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

@PublishedApi
internal val mapper: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .build()

/**
 * Deserializes [ToolRequest.arguments] into [T] using Jackson.
 *
 * Uses [JsonMapper.convertValue] — no JSON string round-trip.
 * Missing fields without defaults throw [IllegalArgumentException].
 *
 * Usage:
 * ```kotlin
 * data class ListTablesArgs(val schemaName: String)
 *
 * handler = ToolHandler { request ->
 *     val args = request.argumentsAs<ListTablesArgs>()
 *     ToolResult(listTables(svc, args.schemaName))
 * }
 * ```
 */
inline fun <reified T : Any> ToolRequest.argumentsAs(): T =
    mapper.convertValue(arguments, T::class.java)





