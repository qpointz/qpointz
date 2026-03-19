package io.qpointz.mill.ai.capabilities.sqlquery

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

import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.executeSql
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.validateSql

/**
 * Dependency carrying concrete validation/execution collaborators into [SqlQueryCapability].
 */
data class SqlQueryCapabilityDependency(
    val validator: SqlQueryToolHandlers.SqlValidationService,
    val executor: SqlQueryToolHandlers.SqlExecutionService,
) : CapabilityDependency

/**
 * Provider for the SQL query capability.
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

    override val tools: List<ToolBinding> = listOf(
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




