package io.qpointz.mill.ai.mcp.transport.http

import dev.langchain4j.model.chat.request.json.JsonArraySchema
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema
import dev.langchain4j.model.chat.request.json.JsonEnumSchema
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema
import dev.langchain4j.model.chat.request.json.JsonNumberSchema
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import dev.langchain4j.model.chat.request.json.JsonStringSchema
import io.modelcontextprotocol.spec.McpSchema

/**
 * Maps LangChain4j tool JSON schemas to MCP wire schemas.
 */
object McpToolSchemaMapper {

  /**
   * Converts a manifest [JsonObjectSchema] into an MCP [McpSchema.JsonSchema].
   *
   * @param schema LangChain4j object schema from capability manifest metadata.
   */
  fun toMcpJsonSchema(schema: JsonObjectSchema): McpSchema.JsonSchema {
    val properties = schema.properties().mapValues { (_, element) -> toMap(element) }
    val definitions = schema.definitions().mapValues { (_, element) -> toMap(element) }
    return McpSchema.JsonSchema(
      "object",
      properties,
      schema.required(),
      schema.additionalProperties() ?: true,
      definitions,
      definitions,
    )
  }

  private fun toMap(element: JsonSchemaElement): Any =
    when (element) {
      is JsonObjectSchema -> buildMap {
        put("type", "object")
        element.description()?.let { put("description", it) }
        put("properties", element.properties().mapValues { (_, value) -> toMap(value) })
        if (element.required().isNotEmpty()) {
          put("required", element.required())
        }
        element.additionalProperties()?.let { put("additionalProperties", it) }
      }
      is JsonStringSchema -> buildMap {
        put("type", "string")
        element.description()?.let { put("description", it) }
      }
      is JsonNumberSchema -> buildMap {
        put("type", "number")
        element.description()?.let { put("description", it) }
      }
      is JsonIntegerSchema -> buildMap {
        put("type", "integer")
        element.description()?.let { put("description", it) }
      }
      is JsonBooleanSchema -> buildMap {
        put("type", "boolean")
        element.description()?.let { put("description", it) }
      }
      is JsonArraySchema -> buildMap {
        put("type", "array")
        element.description()?.let { put("description", it) }
        put("items", toMap(element.items()))
      }
      is JsonEnumSchema -> buildMap {
        put("type", "string")
        put("enum", element.enumValues())
        element.description()?.let { put("description", it) }
      }
      else -> mapOf("type" to "object")
    }
}
