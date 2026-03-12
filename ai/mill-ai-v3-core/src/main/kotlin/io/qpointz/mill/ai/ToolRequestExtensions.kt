package io.qpointz.mill.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule

@PublishedApi
internal val mapper: ObjectMapper = ObjectMapper().registerModule(kotlinModule())

/**
 * Deserializes [ToolRequest.arguments] into [T] using Jackson.
 *
 * Uses [ObjectMapper.convertValue] — no JSON string round-trip.
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
