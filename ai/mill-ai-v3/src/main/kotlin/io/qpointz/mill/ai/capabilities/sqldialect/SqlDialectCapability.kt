package io.qpointz.mill.ai.capabilities.sqldialect

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

import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlDialectConventions
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlFunctionInfo
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlFunctions
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlJoinRules
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlPagingRules
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec

/**
 * Dependency carrying the [SqlDialectSpec] instance into [SqlDialectCapability].
 */
data class SqlDialectCapabilityDependency(val dialectSpec: SqlDialectSpec) : CapabilityDependency

/**
 * Provider for the read-only SQL dialect capability.
 */
class SqlDialectCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "sql-dialect",
        name = "SQL Dialect",
        description = "Read-only SQL dialect conventions and function catalogue for SQL-writing agents",
        supportedContexts = setOf("general"),
        tags = setOf("sql", "dialect"),
        requiredDependencies = setOf(SqlDialectCapabilityDependency::class.java),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = SqlDialectCapability(
        descriptor(),
        dependencies.require(SqlDialectCapabilityDependency::class.java).dialectSpec,
    )
}

private data class SqlDialectCapability(
    override val descriptor: CapabilityDescriptor,
    private val spec: SqlDialectSpec,
) : Capability {

    private data class GetSqlFunctionsArgs(val category: String)
    private data class GetSqlFunctionInfoArgs(val name: String)

    private val manifest = CapabilityManifest.load("capabilities/sql-dialect.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = emptyList()

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("get_sql_dialect_conventions") {
            ToolResult(getSqlDialectConventions(spec))
        },
        manifest.tool("get_sql_paging_rules") {
            ToolResult(getSqlPagingRules(spec))
        },
        manifest.tool("get_sql_join_rules") {
            ToolResult(getSqlJoinRules(spec))
        },
        manifest.tool("get_sql_functions") { request ->
            val args = request.argumentsAs<GetSqlFunctionsArgs>()
            ToolResult(getSqlFunctions(spec, args.category))
        },
        manifest.tool("get_sql_function_info") { request ->
            val args = request.argumentsAs<GetSqlFunctionInfoArgs>()
            ToolResult(getSqlFunctionInfo(spec, args.name))
        },
    )
}




