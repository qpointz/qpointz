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

import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.describeSql
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.executeSqlBounded
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.validateSql

import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec

/**
 * Dependency carrying the SQL **validator** into [SqlQueryCapability].
 *
 * **Host contract:** validated SQL and generated-SQL artifacts are emitted via the agent event stream
 * and routed persistence (see `sql-query` protocols in `capabilities/sql-query.yaml`). A
 * **postprocessor** in the chat service or UI consumes those artifacts and may call an
 * application-side execution service — **not** inside this capability.
 */
data class SqlQueryCapabilityDependency(
    val validator: SqlQueryToolHandlers.SqlValidationService,
    val execution: SqlQueryExecutionPort,
    val defaultMaxRows: Int = SqlQueryExecutionLimits.DEFAULT_MAX_ROWS,
    val hardMaxRows: Int = SqlQueryExecutionLimits.HARD_MAX_ROWS,
    val dialectSpec: SqlDialectSpec? = null,
) : CapabilityDependency

/**
 * Provider for the SQL query capability (**generate / validate** semantics for the agent).
 */
class SqlQueryCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "sql-query",
        name = "SQL Query",
        description = "SQL validation, generated-SQL artifacts, and bounded execution for ai/v3",
        supportedContexts = setOf("general", "analysis"),
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
            execution = dep.execution,
            defaultMaxRows = dep.defaultMaxRows,
            hardMaxRows = dep.hardMaxRows,
            dialectSpec = dep.dialectSpec,
        )
    }
}

private data class SqlQueryCapability(
    override val descriptor: CapabilityDescriptor,
    private val validator: SqlQueryToolHandlers.SqlValidationService,
    private val execution: SqlQueryExecutionPort,
    private val defaultMaxRows: Int,
    private val hardMaxRows: Int,
    private val dialectSpec: SqlDialectSpec?,
) : Capability {

    private data class ValidateSqlArgs(
        val sql: String,
        val attempt: Int = 1,
        val title: String? = null,
        val description: String? = null,
        val completionMode: String? = null,
    )

    private data class DescribeSqlArgs(
        val sql: String,
        val dialect: String? = null,
    )

    private data class ExecuteSqlArgs(
        val sql: String,
        val resultMode: String? = null,
        val max_rows: Int? = null,
        val dialect: String? = null,
        val pageIndex: Int? = null,
        val pageSize: Int? = null,
    )

    private val manifest = CapabilityManifest.load("capabilities/sql-query.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("validate_sql") { request ->
            val args = request.argumentsAs<ValidateSqlArgs>()
            ToolResult(
                validateSql(
                    validator = validator,
                    sql = args.sql,
                    attempt = args.attempt,
                    title = args.title,
                    description = args.description,
                    completionMode = args.completionMode,
                    dialectSpec = dialectSpec,
                ),
            )
        },
        manifest.tool("describe_sql") { request ->
            val args = request.argumentsAs<DescribeSqlArgs>()
            ToolResult(describeSql(execution, args.sql, args.dialect))
        },
        manifest.tool("execute_sql") { request ->
            val args = request.argumentsAs<ExecuteSqlArgs>()
            ToolResult(
                executeSqlBounded(
                    execution = execution,
                    sql = args.sql,
                    resultMode = args.resultMode,
                    maxRows = args.max_rows,
                    dialect = args.dialect,
                    pageIndex = args.pageIndex ?: 0,
                    pageSize = args.pageSize,
                    defaultMaxRows = defaultMaxRows,
                    hardMaxRows = hardMaxRows,
                ),
            )
        },
    )
}
