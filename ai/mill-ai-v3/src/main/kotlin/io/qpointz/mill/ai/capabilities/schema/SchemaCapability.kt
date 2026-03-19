package io.qpointz.mill.ai.capabilities.schema

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

import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listColumns
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listRelations
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listSchemas
import io.qpointz.mill.ai.capabilities.schema.SchemaToolHandlers.listTables
import io.qpointz.mill.data.schema.SchemaFacetService

/**
 * Dependency carrying the [SchemaFacetService] instance into [SchemaCapability].
 */
data class SchemaCapabilityDependency(val schemaFacetService: SchemaFacetService) : CapabilityDependency

/**
 * Provider for the read-only schema exploration capability.
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
 */
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

    override val tools: List<ToolBinding> = listOf(
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




