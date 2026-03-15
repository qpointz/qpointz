package io.qpointz.mill.ai.capabilities.sqlquery

import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityDependencies
import io.qpointz.mill.ai.CapabilityDependency
import io.qpointz.mill.ai.CapabilityDescriptor
import io.qpointz.mill.ai.CapabilityManifest
import io.qpointz.mill.ai.CapabilityProvider
import io.qpointz.mill.ai.PromptAsset
import io.qpointz.mill.ai.ProtocolDefinition
import io.qpointz.mill.ai.ToolResult
import io.qpointz.mill.ai.ToolDefinition
import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.argumentsAs
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.executeSql
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.validateSql

/**
 * Dependency carrying concrete validation/execution collaborators into [SqlQueryCapability].
 *
 * The capability remains framework-free and only knows about these abstract service boundaries.
 */
data class SqlQueryCapabilityDependency(
    val validator: SqlQueryToolHandlers.SqlValidationService,
    val executor: SqlQueryToolHandlers.SqlExecutionService,
) : CapabilityDependency

/**
 * Provider for the SQL query capability.
 *
 * This capability owns validation/execution tools and SQL-related protocol declarations.
 * SQL generation itself remains a structured model action, not an application tool.
 */
class SqlQueryCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "sql-query",
        name = "SQL Query",
        description = "SQL validation and execution capability with structured query artifacts",
        supportedContexts = setOf("general"),
        tags = setOf("sql", "query"),
        requiredDependencies = setOf(SqlQueryCapabilityDependency::class.java),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability {
        val dep = dependencies.require(SqlQueryCapabilityDependency::class.java)
        return SqlQueryCapability(
            descriptor = descriptor(),
            validator = dep.validator,
            executor = dep.executor,
        )
    }
}

/**
 * SQL query capability wiring.
 *
 * Only manifest loading, dependency extraction, and tool binding live here. Pure logic stays in
 * [SqlQueryToolHandlers].
 */
private data class SqlQueryCapability(
    override val descriptor: CapabilityDescriptor,
    private val validator: SqlQueryToolHandlers.SqlValidationService,
    private val executor: SqlQueryToolHandlers.SqlExecutionService,
) : Capability {

    private data class ValidateSqlArgs(
        val sql: String,
        val attempt: Int = 1,
    )

    private data class ExecuteSqlArgs(
        val statementId: String,
        val sql: String,
    )

    private val manifest = CapabilityManifest.load("capabilities/sql-query.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    override val tools: List<ToolDefinition> = listOf(
        manifest.tool("validate_sql") { request ->
            val args = request.argumentsAs<ValidateSqlArgs>()
            ToolResult(validateSql(validator, args.sql, args.attempt))
        },
        manifest.tool("execute_sql") { request ->
            val args = request.argumentsAs<ExecuteSqlArgs>()
            ToolResult(executeSql(executor, args.statementId, args.sql))
        },
    )
}
