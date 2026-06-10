package io.qpointz.mill.ai.core.tool

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

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





