package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectCapabilityDependency
import io.qpointz.mill.ai.core.artifact.ArtifactDescriptor
import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.core.artifact.EmissionStrategy
import io.qpointz.mill.ai.core.artifact.structuredResultMap
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.RunState
import io.qpointz.mill.ai.runtime.events.AgentEvent

/**
 * Registry-driven artefact emission after tool execution.
 *
 * Constructs [AgentEvent.ProtocolFinal] for [EmissionStrategy.ON_TOOL_SUCCESS] without invoking
 * [LangChain4jProtocolExecutor].
 */
class ArtifactEmissionCoordinator(
    private val registry: ArtifactDescriptorRegistry,
) {

    /**
     * Record of a single tool execution within an agent iteration.
     *
     * @param name Tool handler name.
     * @param result Structured tool result payload.
     */
    data class ExecutedTool(
        val name: String,
        val result: Any?,
    )

    /**
     * Evaluates `emitsOnSuccess` triggers for executed tools and emits protocol finals.
     *
     * @param executedTools Tools run in the current planner iteration.
     * @param runState Run state carrying profile and agent context.
     * @param listener Event listener for emitted finals.
     * @return True when at least one protocol final was emitted.
     */
    fun emitOnToolSuccess(
        executedTools: List<ExecutedTool>,
        runState: RunState,
        listener: (AgentEvent) -> Unit,
    ): Boolean {
        var emitted = false
        executedTools.forEach { executed ->
            registry.emitTriggersForTool(executed.name).forEach { trigger ->
                if (!trigger.matches(executed.result)) return@forEach
                val descriptor = registry.descriptorById(trigger.artifactId)
                    ?: error("unknown emit artifact id: ${trigger.artifactId}")
                require(descriptor.emissionStrategy == EmissionStrategy.ON_TOOL_SUCCESS) {
                    "descriptor ${descriptor.id} is not OnToolSuccess"
                }
                val final = constructProtocolFinal(descriptor, executed.result, runState.context)
                if (descriptor.artifactKind == "generated-sql") {
                    val sql = (final.payload as? Map<*, *>)?.get("sql")?.toString().orEmpty()
                    if (sql.isBlank()) return@forEach
                }
                listener(final)
                emitted = true
            }
        }
        return emitted
    }

    /**
     * Builds a [AgentEvent.ProtocolFinal] payload for [descriptor] from a tool result.
     *
     * @param descriptor Target artefact descriptor.
     * @param toolResult Structured tool result map.
     * @param context Agent context for dialect resolution.
     */
    fun constructProtocolFinal(
        descriptor: ArtifactDescriptor,
        toolResult: Any?,
        context: AgentContext,
    ): AgentEvent.ProtocolFinal {
        val protocolId = requireNotNull(descriptor.protocolId) {
            "descriptor ${descriptor.id} requires protocolId"
        }
        val resultMap = structuredResultMap(toolResult) ?: emptyMap()
        val payload = when (descriptor.artifactKind) {
            "generated-sql" -> buildGeneratedSqlPayload(resultMap, context)
            else -> resultMap
        }
        return AgentEvent.ProtocolFinal(protocolId = protocolId, payload = payload)
    }

    private fun buildGeneratedSqlPayload(resultMap: Map<*, *>, context: AgentContext): Map<String, Any?> {
        val sql = resultMap["normalizedSql"]?.toString()?.takeIf { it.isNotBlank() }
            ?: resultMap["sql"]?.toString()
            ?: ""
        return mapOf(
            "artifactType" to "generated-sql",
            "sql" to sql,
            "dialectId" to resolveDialectId(context),
            "statementKind" to inferStatementKind(sql),
            "source" to "generated",
            "validationWarnings" to emptyList<String>(),
        )
    }

    private fun resolveDialectId(context: AgentContext): String =
        context.capabilityDependencies
            .forCapability("sql-dialect")
            ?.get(SqlDialectCapabilityDependency::class.java)
            ?.dialectSpec
            ?.id
            ?: "unknown"

    private fun inferStatementKind(sql: String): String =
        when {
            sql.trimStart().startsWith("SELECT", ignoreCase = true) -> "select"
            sql.trimStart().startsWith("WITH", ignoreCase = true) -> "select"
            else -> "unknown"
        }
}
