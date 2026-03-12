package io.qpointz.mill.ai

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule

// ---------------------------------------------------------------------------
// YAML data models (private)
// ---------------------------------------------------------------------------

private data class ToolSchemaYaml(
    val type: String,
    val description: String? = null,
    val properties: Map<String, ToolSchemaYaml>? = null,
    val items: ToolSchemaYaml? = null,
    val required: List<String>? = null,
    val additionalProperties: Boolean = false,
    val enum: List<String>? = null,
) {
    fun toToolSchema(): ToolSchema = when (ToolSchemaType.valueOf(type.uppercase())) {
        ToolSchemaType.OBJECT -> ToolSchema.obj(
            description = description,
            properties = properties?.map { (name, schema) ->
                ToolSchemaField(
                    name = name,
                    schema = schema.toToolSchema(),
                    required = required?.contains(name) ?: true,
                )
            } ?: emptyList(),
            additionalProperties = additionalProperties,
        )
        ToolSchemaType.ARRAY -> ToolSchema.array(
            items = requireNotNull(items) { "Array schema must define 'items'" }.toToolSchema(),
            description = description,
        )
        ToolSchemaType.STRING  -> ToolSchema.string(description, enum)
        ToolSchemaType.INTEGER -> ToolSchema.integer(description)
        ToolSchemaType.NUMBER  -> ToolSchema.number(description)
        ToolSchemaType.BOOLEAN -> ToolSchema.boolean(description)
    }
}

private data class ToolEntryYaml(
    val description: String,
    val input: ToolSchemaYaml? = null,
    val output: ToolSchemaYaml? = null,
)

private data class PromptEntryYaml(
    val description: String,
    val content: String,
)

private data class CapabilityManifestYaml(
    val name: String,
    val description: String,
    val prompts: Map<String, PromptEntryYaml> = emptyMap(),
    val tools: Map<String, ToolEntryYaml> = emptyMap(),
)

// ---------------------------------------------------------------------------
// Loader
// ---------------------------------------------------------------------------

private val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory()).apply {
    registerModule(kotlinModule())
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

// ---------------------------------------------------------------------------
// CapabilityManifest
// ---------------------------------------------------------------------------

/**
 * Declarative manifest for a capability loaded from a single YAML resource file.
 *
 * One file per capability. All tool descriptions, schemas, and prompt content live here.
 * Handlers are the only imperative part supplied in code.
 *
 * YAML format:
 * ```yaml
 * name: schema
 * description: Schema exploration capability
 *
 * prompts:
 *   schema.system:
 *     description: Guidance for schema exploration.
 *     content: |
 *       Use schema tools to discover the data platform structure.
 *
 * tools:
 *   list_schemas:
 *     description: Return all schemas available in the data platform.
 *     output:
 *       type: array
 *       items:
 *         type: object
 *         properties:
 *           schemaName:
 *             type: string
 *             description: Exact schema name.
 * ```
 *
 * Usage:
 * ```kotlin
 * val manifest = CapabilityManifest.load("capabilities/schema.yaml")
 *
 * override val tools = listOf(
 *     manifest.tool("list_schemas") { ToolResult(listSchemas(svc)) },
 *     manifest.tool("list_tables") { request ->
 *         val args = request.argumentsAs<ListTablesArgs>()
 *         ToolResult(listTables(svc, args.schemaName))
 *     },
 * )
 *
 * override val prompts = manifest.allPrompts
 * ```
 */
class CapabilityManifest private constructor(
    val name: String,
    val description: String,
    private val promptEntries: Map<String, PromptEntryYaml>,
    private val toolEntries: Map<String, ToolEntryYaml>,
) {
    /**
     * Build a [ToolDefinition] for the named tool using the supplied handler.
     * Throws if the tool name is not declared in this manifest.
     */
    fun tool(name: String, handler: ToolHandler): ToolDefinition {
        val entry = toolEntries[name]
            ?: error("Tool '$name' not declared in manifest '$name'")
        return ToolDefinition(
            name = name,
            description = entry.description.trim(),
            inputSchema = entry.input?.toToolSchema() ?: ToolSchema.obj(),
            outputSchema = entry.output?.toToolSchema() ?: ToolSchema.obj(),
            handler = handler,
        )
    }

    /**
     * Return all prompts declared in this manifest as [PromptAsset] instances.
     */
    val allPrompts: List<PromptAsset>
        get() = promptEntries.map { (id, entry) ->
            PromptAsset(id = id, description = entry.description, content = entry.content.trim())
        }

    /**
     * Return a single [PromptAsset] by id.
     * Throws if the id is not declared in this manifest.
     */
    fun promptAsset(id: String): PromptAsset {
        val entry = promptEntries[id]
            ?: error("Prompt '$id' not declared in manifest '$name'")
        return PromptAsset(id = id, description = entry.description, content = entry.content.trim())
    }

    companion object {
        /**
         * Load a [CapabilityManifest] from a classpath resource.
         *
         * @param resource Classpath-relative path, e.g. `"capabilities/schema.yaml"`.
         */
        fun load(resource: String): CapabilityManifest {
            val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resource)
                ?: error("CapabilityManifest resource not found: $resource")
            val yaml = yamlMapper.readValue(stream, CapabilityManifestYaml::class.java)
            return CapabilityManifest(
                name = yaml.name,
                description = yaml.description,
                promptEntries = yaml.prompts,
                toolEntries = yaml.tools,
            )
        }
    }
}
