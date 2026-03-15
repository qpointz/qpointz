package io.qpointz.mill.ai

/**
 * JSON-schema-friendly value types supported by the runtime tool model.
 */
enum class ToolSchemaType {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    OBJECT,
    ARRAY,
}

/**
 * Recursive schema model for tool inputs and outputs.
 *
 * This is intentionally constrained to shapes that can be translated directly into JSON Schema
 * and provider-specific schema models such as LangChain4j's `JsonObjectSchema`.
 */
data class ToolSchema(
    val type: ToolSchemaType,
    val description: String? = null,
    val properties: List<ToolSchemaField> = emptyList(),
    val items: ToolSchema? = null,
    val additionalProperties: Boolean = false,
    val enum: List<String>? = null,
) {
    init {
        require(type == ToolSchemaType.OBJECT || properties.isEmpty()) {
            "Only OBJECT schemas may declare properties."
        }
        require(type == ToolSchemaType.ARRAY || items == null) {
            "Only ARRAY schemas may declare items."
        }
        require(type == ToolSchemaType.STRING || enum == null) {
            "Only STRING schemas may declare enum values."
        }
    }

    companion object {
        fun string(description: String? = null, enum: List<String>? = null): ToolSchema =
            ToolSchema(type = ToolSchemaType.STRING, description = description, enum = enum)

        fun integer(description: String? = null): ToolSchema =
            ToolSchema(type = ToolSchemaType.INTEGER, description = description)

        fun number(description: String? = null): ToolSchema =
            ToolSchema(type = ToolSchemaType.NUMBER, description = description)

        fun boolean(description: String? = null): ToolSchema =
            ToolSchema(type = ToolSchemaType.BOOLEAN, description = description)

        fun obj(
            description: String? = null,
            properties: List<ToolSchemaField> = emptyList(),
            additionalProperties: Boolean = false,
        ): ToolSchema =
            ToolSchema(
                type = ToolSchemaType.OBJECT,
                description = description,
                properties = properties,
                additionalProperties = additionalProperties,
            )

        fun array(
            items: ToolSchema,
            description: String? = null,
        ): ToolSchema =
            ToolSchema(
                type = ToolSchemaType.ARRAY,
                description = description,
                items = items,
            )
    }
}

/**
 * Named field within an object schema.
 */
data class ToolSchemaField(
    val name: String,
    val schema: ToolSchema,
    val required: Boolean = true,
)

/** Runtime request passed to a tool handler. */
data class ToolRequest(
    val arguments: Map<String, Any?> = emptyMap(),
    val context: ToolExecutionContext = ToolExecutionContext(),
)

/** Runtime context passed to a single tool invocation. */
data class ToolExecutionContext(
    val agentContext: AgentContext? = null,
    private val attributes: Map<Class<*>, Any> = emptyMap(),
) {
    fun <T : Any> get(type: Class<T>): T? = type.cast(attributes[type])

    fun <T : Any> require(type: Class<T>): T =
        requireNotNull(get(type)) { "Missing tool execution attribute: ${type.name}" }

    companion object {
        fun of(agentContext: AgentContext? = null, vararg attributes: Any): ToolExecutionContext =
            ToolExecutionContext(
                agentContext = agentContext,
                attributes = attributes.associateBy { it.javaClass },
            )
    }
}

/** Runtime response returned by a tool handler. */
data class ToolResult(
    val content: Any? = null,
)

/** Functional adapter so trivial tools can be declared inline. */
fun interface ToolHandler {
    fun invoke(request: ToolRequest): ToolResult
}

/**
 * Classifies a tool by its runtime role.
 *
 * - [QUERY]   — read-only; the result informs the next planning step (default).
 * - [CAPTURE] — side-effecting; the tool produces a terminal artifact. The observer
 *               should treat a completed CAPTURE call as ready for synthesis.
 */
enum class ToolKind { QUERY, CAPTURE }

/**
 * Adapter-agnostic tool definition with explicit JSON-schema-friendly request and response models.
 */
data class ToolDefinition(
    val name: String,
    val description: String,
    val inputSchema: ToolSchema = ToolSchema.obj(),
    val outputSchema: ToolSchema,
    val handler: ToolHandler,
    val kind: ToolKind = ToolKind.QUERY,
) {
    companion object
}
