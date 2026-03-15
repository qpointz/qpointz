package io.qpointz.mill.ai.capabilities.sqldialect

import io.qpointz.mill.ai.*
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlDialectConventions
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlFunctionInfo
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlFunctions
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlJoinRules
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectToolHandlers.getSqlPagingRules
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec

/**
 * Dependency carrying the [SqlDialectSpec] instance into [SqlDialectCapability].
 *
 * The dialect spec is injected rather than looked up directly so that the capability
 * stays free from Spring and service-locator coupling. The caller (typically the agent
 * entry point) selects and supplies the dialect.
 */
data class SqlDialectCapabilityDependency(val dialectSpec: SqlDialectSpec) : CapabilityDependency

/**
 * Provider for the read-only SQL dialect capability.
 *
 * Declares [SqlDialectCapabilityDependency] as a required dependency so the registry rejects
 * profile configurations that forget to supply a [SqlDialectSpec].
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

/**
 * Read-only SQL dialect capability.
 *
 * Contributes five grounding tools for SQL-writing agents. No protocols are declared
 * because this capability is a pure grounding supplier — the synthesising protocol is
 * owned by the composing profile.
 *
 * Recommended tool chain:
 * 1. `get_sql_dialect_conventions` — discover identifier rules, literal rules, and function categories.
 * 2. `get_sql_paging_rules` / `get_sql_join_rules` — fetch topic-specific conventions as needed.
 * 3. `get_sql_functions(category)` — list functions in a category.
 * 4. `get_sql_function_info(name)` — get full overload detail for a chosen function.
 */
private data class SqlDialectCapability(
    override val descriptor: CapabilityDescriptor,
    private val spec: SqlDialectSpec,
) : Capability {

    private data class GetSqlFunctionsArgs(val category: String)
    private data class GetSqlFunctionInfoArgs(val name: String)

    private val manifest = CapabilityManifest.load("capabilities/sql-dialect.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = emptyList()

    override val tools: List<ToolDefinition> = listOf(
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
