package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.ai.*
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listColumns
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listRelations
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listSchemas
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listTables
import io.qpointz.mill.data.schema.SchemaFacetService

/**
 * Dependency carrying the [SchemaFacetService] instance into [SchemaCapability].
 *
 * [SchemaFacetService] is injected rather than looked up directly because the schema
 * capability is instantiated per-run by the [CapabilityRegistry]. The registry delegates
 * dependency resolution to the caller (typically the agent entry point), keeping the
 * capability itself free from Spring and service-locator coupling.
 */
data class SchemaCapabilityDependency(val schemaFacetService: SchemaFacetService) : CapabilityDependency

/**
 * Provider for the read-only schema exploration capability.
 *
 * Declares [SchemaCapabilityDependency] as a required dependency so the registry rejects
 * profile configurations that forget to supply a [SchemaFacetService].
 */
class SchemaCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "schema",
        name = "Schema",
        description = "Providing schema and metadata capabilities",
        supportedContexts = setOf("general"),
        tags = setOf("schema"),
        requiredDependencies = setOf(SchemaCapabilityDependency::class.java),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = SchemaCapability(
        descriptor(),
        dependencies.require(SchemaCapabilityDependency::class.java).schemaFacetService,
    )
}

/**
 * Read-only schema exploration capability.
 *
 * Contributes four grounding tools and one system prompt. No protocols are declared because
 * the schema capability is a pure grounding supplier — the protocol for the final answer is
 * owned by whichever profile composes this capability (typically `conversation.stream` from
 * the conversation capability, or `schema-authoring.capture` from the authoring capability).
 *
 * Tool grounding chain: `list_schemas` → `list_tables` → `list_columns` | `list_relations`.
 * The planner should always call `list_schemas` first when schema names are not yet known.
 */
private data class SchemaCapability(
    override val descriptor: CapabilityDescriptor,
    private val svc: SchemaFacetService,
) : Capability {

    /** Typed argument class for [listTables] — schema scoping. */
    private data class ListTablesArgs(val schemaName: String)

    /** Typed argument class for [listColumns] — table scoping within a schema. */
    private data class ListColumnsArgs(val schemaName: String, val tableName: String)

    /**
     * Typed argument class for [listRelations].
     *
     * [direction] defaults to [RelationDirection.BOTH] so the planner can omit it for a
     * full relation scan. The enum is declared in the YAML manifest so the LLM receives a
     * closed set of valid values.
     */
    private data class ListRelationsArgs(
        val schemaName: String,
        val tableName: String,
        val direction: RelationDirection = RelationDirection.BOTH,
    )

    private val manifest = CapabilityManifest.load("capabilities/schema.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    /**
     * No protocols declared. The schema capability is grounding-only — it does not own
     * any synthesis or output contract. The composing profile supplies the active protocol.
     */
    override val protocols: List<ProtocolDefinition> = emptyList()

    override val tools: List<ToolDefinition> = listOf(
        manifest.tool("list_schemas") {
            ToolResult(listSchemas(svc))
        },
        manifest.tool("list_tables") { request ->
            val args = request.argumentsAs<ListTablesArgs>()
            ToolResult(listTables(svc, args.schemaName))
        },
        manifest.tool("list_columns") { request ->
            val args = request.argumentsAs<ListColumnsArgs>()
            ToolResult(listColumns(svc, args.schemaName, args.tableName))
        },
        manifest.tool("list_relations") { request ->
            val args = request.argumentsAs<ListRelationsArgs>()
            ToolResult(listRelations(svc, args.schemaName, args.tableName, args.direction))
        },
    )
}
