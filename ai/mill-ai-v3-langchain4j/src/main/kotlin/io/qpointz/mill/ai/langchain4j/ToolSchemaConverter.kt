package io.qpointz.mill.ai.langchain4j

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.model.chat.request.json.JsonArraySchema
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema
import dev.langchain4j.model.chat.request.json.JsonEnumSchema
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema
import dev.langchain4j.model.chat.request.json.JsonNumberSchema
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import dev.langchain4j.model.chat.request.json.JsonStringSchema
import io.qpointz.mill.ai.ToolDefinition
import io.qpointz.mill.ai.ToolSchema
import io.qpointz.mill.ai.ToolSchemaType

/**
 * Converts the framework-free [ToolSchema] model into LangChain4j JSON schema types.
 *
 * Extracted from agent wiring so it can be tested independently of any LLM or streaming runtime.
 */
internal object ToolSchemaConverter {

    fun toToolSpecification(tool: ToolDefinition): ToolSpecification =
        ToolSpecification.builder()
            .name(tool.name)
            .description(tool.description)
            .parameters(toJsonObjectSchema(tool.inputSchema))
            .build()

    fun toJsonObjectSchema(schema: ToolSchema): JsonObjectSchema {
        require(schema.type == ToolSchemaType.OBJECT) {
            "LangChain4j tool parameter schema must be OBJECT, got ${schema.type}"
        }

        val builder = JsonObjectSchema.builder()
        schema.description?.let(builder::description)

        schema.properties.forEach { field ->
            when (field.schema.type) {
                ToolSchemaType.STRING ->
                    if (!field.schema.enum.isNullOrEmpty())
                        builder.addProperty(
                            field.name,
                            JsonEnumSchema.builder()
                                .enumValues(field.schema.enum)
                                .description(field.schema.description)
                                .build()
                        )
                    else
                        builder.addStringProperty(field.name, field.schema.description)
                ToolSchemaType.INTEGER ->
                    builder.addIntegerProperty(field.name, field.schema.description)
                ToolSchemaType.NUMBER ->
                    builder.addNumberProperty(field.name, field.schema.description)
                ToolSchemaType.BOOLEAN ->
                    builder.addBooleanProperty(field.name, field.schema.description)
                ToolSchemaType.OBJECT ->
                    builder.addProperty(field.name, toJsonObjectSchema(field.schema))
                ToolSchemaType.ARRAY ->
                    builder.addProperty(field.name, toJsonSchemaElement(field.schema))
            }
        }

        val requiredFields = schema.properties.filter { it.required }.map { it.name }
        if (requiredFields.isNotEmpty()) {
            builder.required(requiredFields)
        }

        builder.additionalProperties(schema.additionalProperties)
        return builder.build()
    }

    fun toJsonSchemaElement(schema: ToolSchema): JsonSchemaElement =
        when (schema.type) {
            ToolSchemaType.STRING ->
                if (!schema.enum.isNullOrEmpty())
                    JsonEnumSchema.builder()
                        .enumValues(schema.enum)
                        .description(schema.description)
                        .build()
                else
                    JsonStringSchema.builder()
                        .description(schema.description)
                        .build()
            ToolSchemaType.INTEGER ->
                JsonIntegerSchema.builder()
                    .description(schema.description)
                    .build()
            ToolSchemaType.NUMBER ->
                JsonNumberSchema.builder()
                    .description(schema.description)
                    .build()
            ToolSchemaType.BOOLEAN ->
                JsonBooleanSchema.builder()
                    .description(schema.description)
                    .build()
            ToolSchemaType.OBJECT ->
                toJsonObjectSchema(schema)
            ToolSchemaType.ARRAY ->
                JsonArraySchema.builder()
                    .description(schema.description)
                    .items(toJsonSchemaElement(requireNotNull(schema.items) {
                        "ARRAY schema requires items"
                    }))
                    .build()
        }
}
