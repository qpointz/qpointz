package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.ai.*
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listColumns
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listRelations
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listSchemas
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listTables
import io.qpointz.mill.data.schema.SchemaFacetService

data class SchemaCapabilityDependency(val schemaFacetService: SchemaFacetService) : CapabilityDependency

class SchemaCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "schema",
        name = "Schema",
        description = "Providing schema and metadata capabilities",
        supportedContexts = setOf("general"),
        tags = setOf("schema"),
        requiredDependencies = setOf(SchemaCapabilityDependency::class.java)
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = SchemaCapability(
        descriptor(),
        dependencies.require(SchemaCapabilityDependency::class.java).schemaFacetService,
    )
}

private data class SchemaCapability(
    override val descriptor: CapabilityDescriptor,
    private val svc: SchemaFacetService,
) : Capability {

    private data class ListTablesArgs(val schemaName: String)
    private data class ListColumnsArgs(val schemaName: String, val tableName: String)
    private data class ListRelationsArgs(
        val schemaName: String,
        val tableName: String,
        val direction: RelationDirection = RelationDirection.BOTH,
    )

    private val manifest = CapabilityManifest.load("capabilities/schema.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

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
