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

import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.validateSql

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
) : CapabilityDependency

/**
 * Provider for the SQL query capability (**generate / validate** semantics for the agent).
 */
class SqlQueryCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "sql-query",
        name = "SQL Query",
        description = "SQL validation and generated-SQL artifacts for ai/v3 (execution is host-side)",
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
        )
    }
}

private data class SqlQueryCapability(
    override val descriptor: CapabilityDescriptor,
    private val validator: SqlQueryToolHandlers.SqlValidationService,
) : Capability {

    private data class ValidateSqlArgs(
        val sql: String,
        val attempt: Int = 1,
    )

    private val manifest = CapabilityManifest.load("capabilities/sql-query.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("validate_sql") { request ->
            val args = request.argumentsAs<ValidateSqlArgs>()
            ToolResult(validateSql(validator, args.sql, args.attempt))
        },
    )
}
