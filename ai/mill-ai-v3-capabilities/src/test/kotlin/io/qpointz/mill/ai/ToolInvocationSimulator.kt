package io.qpointz.mill.ai

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule

/**
 * Simulates the JSON-in / JSON-out tool invocation path that the LLM runtime takes.
 *
 * Replicates exactly what the LangChain4j agent's tool invocation path does, without requiring a
 * running agent or any LangChain4j dependency:
 *
 * 1. Parse raw JSON argument string → `Map<String, Any?>`
 * 2. Wrap in `ToolRequest`
 * 3. Invoke `ToolDefinition.handler`
 * 4. Serialize `ToolResult.content` back to JSON
 *
 * Use this in tool-contract tests to prove the full invocation chain — including
 * `argumentsAs<T>()` deserialization, enum coercion, and default values — rather than
 * calling handler functions directly with typed arguments.
 */
object ToolInvocationSimulator {

    private val mapper = ObjectMapper().registerModule(kotlinModule())

    /**
     * Invoke [tool] with a raw JSON argument string, return the serialized JSON result.
     *
     * Pass an empty string or blank string for tools that take no input.
     */
    fun invoke(tool: ToolDefinition, jsonArgs: String = ""): String {
        val arguments: Map<String, Any?> = if (jsonArgs.isBlank()) emptyMap()
        else mapper.readValue(jsonArgs, object : TypeReference<Map<String, Any?>>() {})
        val result = tool.handler.invoke(ToolRequest(arguments = arguments))
        return mapper.writeValueAsString(result.content)
    }

    /**
     * Parse the JSON result string back into a list of maps.
     * Convenience for tools that return an array output.
     */
    fun parseList(json: String): List<Map<String, Any?>> =
        mapper.readValue(json, object : TypeReference<List<Map<String, Any?>>>() {})

    /**
     * Parse the JSON result string back into a single map.
     * Convenience for tools that return an object output.
     */
    fun parseMap(json: String): Map<String, Any?> =
        mapper.readValue(json, object : TypeReference<Map<String, Any?>>() {})
}
