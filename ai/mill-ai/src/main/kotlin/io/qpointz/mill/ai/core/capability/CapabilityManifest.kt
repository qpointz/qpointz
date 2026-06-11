package io.qpointz.mill.ai.core.capability

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
import io.qpointz.mill.ai.core.artifact.ArtifactDescriptor
import io.qpointz.mill.ai.core.artifact.ArtifactSourceEvent
import io.qpointz.mill.ai.core.artifact.EmissionStrategy
import io.qpointz.mill.ai.core.artifact.ToolEmitTrigger

import tools.jackson.databind.DeserializationFeature
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.kotlinModule
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.model.chat.request.json.JsonArraySchema
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema
import dev.langchain4j.model.chat.request.json.JsonEnumSchema
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema
import dev.langchain4j.model.chat.request.json.JsonNumberSchema
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import dev.langchain4j.model.chat.request.json.JsonStringSchema

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
    fun toJsonSchemaElement(): JsonSchemaElement = when (type.uppercase()) {
        "OBJECT" -> toJsonObjectSchema()
        "ARRAY" -> JsonArraySchema.builder()
            .description(description)
            .items(requireNotNull(items) { "Array schema must define 'items'" }.toJsonSchemaElement())
            .build()
        "STRING" -> if (!enum.isNullOrEmpty())
            JsonEnumSchema.builder()
                .enumValues(enum)
                .description(description)
                .build()
        else
            JsonStringSchema.builder()
                .description(description)
                .build()
        "INTEGER" -> JsonIntegerSchema.builder()
            .description(description)
            .build()
        "NUMBER" -> JsonNumberSchema.builder()
            .description(description)
            .build()
        "BOOLEAN" -> JsonBooleanSchema.builder()
            .description(description)
            .build()
        else -> error("Unsupported schema type: $type")
    }

    fun toJsonObjectSchema(): JsonObjectSchema {
        val builder = JsonObjectSchema.builder()
        description?.let(builder::description)
        val requiredFields = required ?: emptyList()
        properties?.forEach { (name, schema) ->
            builder.addProperty(name, schema.toJsonSchemaElement())
        }
        if (requiredFields.isNotEmpty()) {
            builder.required(requiredFields)
        }
        builder.additionalProperties(additionalProperties)
        return builder.build()
    }
}

private data class EmitWhenYaml(
    val field: String,
    val equals: Any? = null,
)

private data class EmitOnSuccessYaml(
    val artifact: String,
    val `when`: EmitWhenYaml? = null,
)

private data class ToolEntryYaml(
    val description: String,
    val kind: String? = null,
    val protocol: String? = null,
    val input: ToolSchemaYaml? = null,
    val output: ToolSchemaYaml? = null,
    val emitsOnSuccess: EmitOnSuccessYaml? = null,
)

private data class ArtifactEntryYaml(
    val protocolId: String? = null,
    val artifactKind: String? = null,
    val persistKind: String? = null,
    val pointerKeys: List<String>? = null,
    val wirePartType: String? = null,
    val presentation: String? = null,
    val protocolMode: String? = null,
    val sourceEvent: String? = null,
    val emissionStrategy: String? = null,
    val destinations: List<String>? = null,
)

private data class PromptEntryYaml(
    val description: String,
    val content: String,
)

private data class ProtocolEventEntryYaml(
    val description: String,
    val payloadSchema: ToolSchemaYaml = ToolSchemaYaml(type = "object"),
)

private data class ProtocolEntryYaml(
    val description: String,
    val mode: String,
    val fallbackMode: String? = null,
    val finalSchema: ToolSchemaYaml? = null,
    val events: Map<String, ProtocolEventEntryYaml> = emptyMap(),
) {
    fun toProtocolDefinition(id: String): ProtocolDefinition = ProtocolDefinition(
        id = id,
        description = description,
        mode = ProtocolMode.valueOf(mode.uppercase()),
        fallbackMode = fallbackMode?.let { ProtocolMode.valueOf(it.uppercase()) },
        finalSchema = finalSchema?.toJsonObjectSchema(),
        events = events.map { (type, entry) ->
            ProtocolEventDefinition(
                type = type,
                description = entry.description,
                payloadSchema = entry.payloadSchema.toJsonObjectSchema(),
            )
        },
    )
}

private data class CapabilityManifestYaml(
    val name: String,
    val description: String,
    val prompts: Map<String, PromptEntryYaml> = emptyMap(),
    val tools: Map<String, ToolEntryYaml> = emptyMap(),
    val protocols: Map<String, ProtocolEntryYaml> = emptyMap(),
    val artifacts: Map<String, ArtifactEntryYaml> = emptyMap(),
)

// ---------------------------------------------------------------------------
// Loader
// ---------------------------------------------------------------------------

private val yamlMapper: YAMLMapper = YAMLMapper.builder()
    .addModule(kotlinModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .build()

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
 * ```
 *
 * Usage:
 * ```kotlin
 * val manifest = CapabilityManifest.load("capabilities/schema.yaml")
 *
 * override val tools = listOf(
 *     manifest.tool("list_schemas", handler = { ToolResult(listSchemas(svc)) }),
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
    private val protocolEntries: Map<String, ProtocolEntryYaml>,
    val artifactDescriptors: List<ArtifactDescriptor>,
    val toolEmitTriggers: Map<String, List<ToolEmitTrigger>>,
) {
    /**
     * Build a [ToolBinding] for the named tool using the supplied handler.
     * Throws if the tool name is not declared in this manifest.
     *
     * The [kindOverride] parameter is a fallback: if the manifest YAML declares a `kind` field for
     * the tool, that takes precedence. Otherwise [kindOverride] (defaulting to [ToolKind.QUERY])
     * is used.
     */
    fun tool(name: String, kindOverride: ToolKind = ToolKind.QUERY, handler: ToolHandler): ToolBinding {
        val entry = toolEntries[name]
            ?: error("Tool '$name' not declared in manifest '${this.name}'")
        val resolvedKind = entry.kind?.let { ToolKind.valueOf(it.uppercase()) } ?: kindOverride
        val spec = ToolSpecification.builder()
            .name(name)
            .description(entry.description.trim())
            .parameters(entry.input?.toJsonObjectSchema() ?: JsonObjectSchema.builder().build())
            .build()
        return ToolBinding(spec = spec, handler = handler, kind = resolvedKind, protocolId = entry.protocol)
    }

    /**
     * Return all protocols declared in this manifest as [ProtocolDefinition] instances.
     */
    val allProtocols: List<ProtocolDefinition>
        get() = protocolEntries.map { (id, entry) -> entry.toProtocolDefinition(id) }

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
            ?: error("Prompt '$id' not declared in manifest '${this.name}'")
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
            val artifacts = yaml.artifacts.map { (id, entry) ->
                entry.toDescriptor(id = id, capabilityId = yaml.name)
            }
            val emitTriggers = yaml.tools.mapNotNull { (toolName, entry) ->
                entry.emitsOnSuccess?.let { emit ->
                    toolName to listOf(
                        ToolEmitTrigger(
                            toolName = toolName,
                            artifactId = emit.artifact,
                            whenField = emit.`when`?.field,
                            equals = emit.`when`?.equals,
                        ),
                    )
                }
            }.toMap()
            return CapabilityManifest(
                name = yaml.name,
                description = yaml.description,
                promptEntries = yaml.prompts,
                toolEntries = yaml.tools,
                protocolEntries = yaml.protocols,
                artifactDescriptors = artifacts,
                toolEmitTriggers = emitTriggers,
            )
        }
    }
}

private fun ArtifactEntryYaml.toDescriptor(id: String, capabilityId: String): ArtifactDescriptor {
    val source = requireNotNull(sourceEvent) { "artifacts.$id requires sourceEvent" }
    val strategy = requireNotNull(emissionStrategy) { "artifacts.$id requires emissionStrategy" }
    val kind = requireNotNull(artifactKind) { "artifacts.$id requires artifactKind" }
    val persist = requireNotNull(persistKind) { "artifacts.$id requires persistKind" }
    val dest = requireNotNull(destinations) { "artifacts.$id requires destinations" }
        .takeIf { it.isNotEmpty() } ?: error("artifacts.$id requires at least one destination")
    val parsedSource = ArtifactSourceEvent.fromYaml(source)
    if (parsedSource == ArtifactSourceEvent.PROTOCOL_FINAL) {
        require(protocolId != null) { "artifacts.$id requires protocolId when sourceEvent is protocol.final" }
    }
    return ArtifactDescriptor(
        id = id,
        capabilityId = capabilityId,
        protocolId = protocolId,
        artifactKind = kind,
        persistKind = persist,
        pointerKeys = pointerKeys?.toSet() ?: emptySet(),
        wirePartType = wirePartType,
        presentation = presentation,
        protocolMode = protocolMode?.let { ProtocolMode.valueOf(it.uppercase()) },
        sourceEvent = parsedSource,
        emissionStrategy = EmissionStrategy.fromYaml(strategy),
        destinations = dest.map { RoutedEventDestination.valueOf(it) }.toSet(),
    )
}





