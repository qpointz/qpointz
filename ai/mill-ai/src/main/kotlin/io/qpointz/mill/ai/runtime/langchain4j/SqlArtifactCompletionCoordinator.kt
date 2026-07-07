package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.core.artifact.structuredResultMap
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.runtime.RunState
import io.qpointz.mill.ai.runtime.events.AgentEvent
import java.util.UUID

/**
 * Turn-scoped SQL artifact completion coordinator (WI-367).
 *
 * Registers completion plans from `completionMode` or `enrich-existing` entry, merges pure tool
 * results into drafts, and emits [AgentEvent.ProtocolFinal] only when [CompletionPlanRegistry.Plan.canFinalize].
 */
class SqlArtifactCompletionCoordinator(
    private val registry: ArtifactDescriptorRegistry,
) {

    /**
     * Result of processing one tool batch within an agent iteration.
     *
     * @param shouldEndTurn when true, the agent should stop the turn after emitting finals
     * @param emittedCount number of protocol finals emitted
     */
    data class BatchResult(
        val shouldEndTurn: Boolean,
        val emittedCount: Int,
    )

    private val activePlans = mutableListOf<CompletionPlanRegistry.Plan>()

    /**
     * Clears turn-scoped plan state. Call at the start of each agent run.
     */
    fun reset() {
        activePlans.clear()
    }

    /**
     * Processes executed tools in order, updating plans and emitting finals when satisfied.
     *
     * @param executedTools tools run in the current iteration
     * @param runState run state with profile and context
     * @param artifactStore durable store for enrich-existing loads
     * @param pointerStore active pointer store for `last-sql` resolution
     * @param listener event listener for protocol finals
     */
    fun processBatch(
        executedTools: List<ArtifactEmissionCoordinator.ExecutedTool>,
        runState: RunState,
        artifactStore: ArtifactStore,
        pointerStore: ActiveArtifactPointerStore,
        listener: (AgentEvent) -> Unit,
    ): BatchResult {
        var emitted = 0
        executedTools.forEach { executed ->
            when (executed.name) {
                "validate_sql" -> handleValidateSql(executed, runState)
                "describe_sql" -> handleDescribeSql(executed)
                "validate_chart_spec" -> handleValidateChartSpec(
                    executed,
                    runState,
                    artifactStore,
                    pointerStore,
                )
            }
        }

        val finalized = activePlans.filter { it.canFinalize() }.toList()
        finalized.forEach { plan ->
            val final = buildProtocolFinal(plan, runState)
            listener(final)
            emitted++
        }
        activePlans.removeAll(finalized.toSet())

        val shouldEndTurn = emitted > 0
        return BatchResult(shouldEndTurn = shouldEndTurn, emittedCount = emitted)
    }

    private fun handleValidateSql(
        executed: ArtifactEmissionCoordinator.ExecutedTool,
        runState: RunState,
    ) {
        val map = structuredResultMap(executed.result) ?: return
        if (map["passed"] != true) return
        val completionMode = CompletionPlanRegistry.Mode.fromWire(map["completionMode"]?.toString())
        if (completionMode == CompletionPlanRegistry.Mode.ENRICH_EXISTING) return
        val normalizedSql = map["normalizedSql"]?.toString()?.takeIf { it.isNotBlank() } ?: return
        val title = map["title"]?.toString().orEmpty()
        val description = map["description"]?.toString().orEmpty()
        val sqlKey = normalizedSql.trim().lowercase()
        if (activePlans.any { it.normalizedSqlKey == sqlKey }) return
        val draft = GeneratedSqlPayload.seedFromValidation(
            normalizedSql = normalizedSql,
            title = title,
            description = description,
            context = runState.context,
        )
        val plan = CompletionPlanRegistry.Plan(
            id = UUID.randomUUID().toString(),
            mode = completionMode,
            steps = CompletionPlanRegistry.stepsFor(completionMode).toMutableList(),
            draft = draft,
            normalizedSqlKey = sqlKey,
        )
        plan.markStepSucceeded("validate_sql")
        activePlans += plan
    }

    private fun handleDescribeSql(executed: ArtifactEmissionCoordinator.ExecutedTool) {
        val map = structuredResultMap(executed.result) ?: return
        val schema = (map["schema"] as? List<*>)?.mapNotNull { row ->
            (row as? Map<*, *>)?.entries?.associate { (k, v) -> k.toString() to v }
        } ?: emptyList()
        if (schema.isEmpty()) {
            activePlans.forEach { plan ->
                if (plan.mode == CompletionPlanRegistry.Mode.SQL_WITH_CHART) {
                    plan.markStepFailed("describe_sql")
                }
            }
            return
        }
        val submittedSqlKey = map["sql"]?.toString()?.trim()?.lowercase().orEmpty()
        val plan = findPlanForSql(submittedSqlKey) ?: soleOpenDescribePlan() ?: return
        plan.draft.schema = schema
        plan.markStepSucceeded("describe_sql")
    }

    /** Single plan awaiting schema probe — used when describe_sql echoes the submitted arg for matching only. */
    private fun soleOpenDescribePlan(): CompletionPlanRegistry.Plan? {
        val awaiting = activePlans.filter { plan ->
            !plan.terminal &&
                plan.mode != CompletionPlanRegistry.Mode.SQL_ONLY &&
                plan.steps.firstOrNull { it.toolName == "describe_sql" }?.status ==
                CompletionPlanRegistry.StepStatus.PENDING &&
                plan.steps.firstOrNull { it.toolName == "validate_sql" }?.status ==
                CompletionPlanRegistry.StepStatus.SUCCEEDED
        }
        return awaiting.singleOrNull()
    }

    private fun handleValidateChartSpec(
        executed: ArtifactEmissionCoordinator.ExecutedTool,
        runState: RunState,
        artifactStore: ArtifactStore,
        pointerStore: ActiveArtifactPointerStore,
    ) {
        val map = structuredResultMap(executed.result) ?: return
        val targetArtifactId = map["targetArtifactId"]?.toString()?.takeIf { it.isNotBlank() }
        var plan = findOpenChartPlan(targetArtifactId)
        if (plan == null && shouldOpenEnrichExisting(map)) {
            plan = openEnrichExistingPlan(map, runState, artifactStore, pointerStore, targetArtifactId)
        }
        if (plan == null) {
            plan = findPlanForSchemaColumnNames(readSchemaColumnNames(map))
        }
        if (plan == null) {
            plan = soleOpenChartPlan()
        }
        plan ?: return
        if (map["passed"] != true) {
            plan.markStepFailed("validate_chart_spec")
            return
        }
        @Suppress("UNCHECKED_CAST")
        val visualization = map["normalizedVisualization"] as? Map<String, Any?> ?: return
        val key = visualization["key"]?.toString() ?: "default"
        plan.draft.visualizations[key] = visualization
        plan.markStepSucceeded("validate_chart_spec")
    }

    /**
     * Enrich-existing is explicit only — never infer from [last-sql] for unrelated new questions.
     */
    private fun shouldOpenEnrichExisting(toolResult: Map<*, *>): Boolean =
        toolResult["opensEnrichPlan"] == true ||
            !toolResult["targetArtifactId"]?.toString().isNullOrBlank()

    private fun openEnrichExistingPlan(
        toolResult: Map<*, *>,
        runState: RunState,
        artifactStore: ArtifactStore,
        pointerStore: ActiveArtifactPointerStore,
        targetArtifactId: String?,
    ): CompletionPlanRegistry.Plan? {
        val conversationId = runState.conversationId ?: return null
        val artifactId = targetArtifactId
            ?: pointerStore.find(conversationId, "last-sql")?.artifactId
            ?: return null
        val record = artifactStore.findById(artifactId) ?: return null
        val inner = extractInnerPayload(record.payload) ?: return null
        val draft = GeneratedSqlPayload.fromPersisted(inner)
        val sqlText = draft.sql["text"]?.toString().orEmpty()
        val schemaPresent = draft.schema.isNotEmpty()
        val plan = CompletionPlanRegistry.Plan(
            id = UUID.randomUUID().toString(),
            mode = CompletionPlanRegistry.Mode.ENRICH_EXISTING,
            steps = CompletionPlanRegistry.stepsFor(
                CompletionPlanRegistry.Mode.ENRICH_EXISTING,
                schemaAlreadyPresent = schemaPresent,
            ).toMutableList(),
            draft = draft,
            targetArtifactId = artifactId,
            normalizedSqlKey = sqlText.trim().lowercase(),
        )
        if (schemaPresent) {
            plan.steps.firstOrNull { it.toolName == "describe_sql" }?.status =
                CompletionPlanRegistry.StepStatus.SUCCEEDED
        }
        activePlans += plan
        return plan
    }

    private fun findOpenChartPlan(targetArtifactId: String?): CompletionPlanRegistry.Plan? {
        if (targetArtifactId.isNullOrBlank()) return null
        return activePlans.firstOrNull {
            it.targetArtifactId == targetArtifactId && !it.terminal
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readSchemaColumnNames(toolResult: Map<*, *>): List<String> =
        (toolResult["schemaColumnNames"] as? List<*>)
            ?.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
            ?.sorted()
            ?: emptyList()

    private fun findPlanForSchemaColumnNames(columnNames: List<String>): CompletionPlanRegistry.Plan? {
        if (columnNames.isEmpty()) return null
        return activePlans.firstOrNull { plan ->
            !plan.terminal &&
                plan.mode != CompletionPlanRegistry.Mode.SQL_ONLY &&
                sortedSchemaColumnNames(plan.draft.schema) == columnNames
        }
    }

    /** Single open chart plan — safe fallback for chart retries in the same turn. */
    private fun soleOpenChartPlan(): CompletionPlanRegistry.Plan? {
        val open = activePlans.filter { !it.terminal && it.mode != CompletionPlanRegistry.Mode.SQL_ONLY }
        return open.singleOrNull()
    }

    private fun sortedSchemaColumnNames(schema: List<Map<String, Any?>>): List<String> =
        schema.mapNotNull { it["name"]?.toString()?.trim()?.takeIf(String::isNotBlank) }.sorted()

    private fun findPlanForSql(normalizedSql: String): CompletionPlanRegistry.Plan? {
        if (normalizedSql.isBlank()) return null
        return activePlans.firstOrNull {
            it.normalizedSqlKey == normalizedSql && !it.terminal
        }
    }

    private fun buildProtocolFinal(
        plan: CompletionPlanRegistry.Plan,
        runState: RunState,
    ): AgentEvent.ProtocolFinal {
        val descriptor = registry.descriptorByQualifiedId("sql-query.generated-sql")
            ?: error("missing sql-query.generated-sql descriptor")
        val payload = plan.draft.toPayloadMap()
        return AgentEvent.ProtocolFinal(
            protocolId = descriptor.protocolId!!,
            payload = payload,
            persistArtifactId = plan.targetArtifactId,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractInnerPayload(content: Map<String, Any?>): Map<String, Any?>? {
        var current: Map<String, Any?> = content
        repeat(4) {
            if (current["artifactType"] != null || current["sql"] != null) return current
            val nested = current["payload"]
            if (nested is Map<*, *>) {
                current = nested as Map<String, Any?>
            } else {
                return current
            }
        }
        return current
    }
}
