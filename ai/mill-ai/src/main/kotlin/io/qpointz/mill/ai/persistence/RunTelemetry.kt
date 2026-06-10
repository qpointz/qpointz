package io.qpointz.mill.ai.persistence

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

import java.util.concurrent.ConcurrentHashMap

/** Accumulated token and tool-call counts for a single agent run. */
data class RunTelemetry(
    val runId: String,
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val totalTokens: Int = 0,
    val toolCallCount: Int = 0,
)

/**
 * In-memory telemetry listener that accumulates per-run statistics from routed events.
 *
 * Listens on the `TELEMETRY` destination lane and aggregates:
 * - input/output/total token counts from `llm.call.completed`
 * - tool call counts from `tool.call`
 *
 * Register on [AgentEventPublisher] via [AgentPersistenceContext].
 */
class RunTelemetryAccumulator : AgentEventListener {

    private val stats = ConcurrentHashMap<String, RunTelemetry>()

    override fun onEvent(event: RoutedAgentEvent) {
        if (!event.destinations.contains(RoutedEventDestination.TELEMETRY)) return
        val runId = event.runId ?: return

        when (event.runtimeType) {
            "llm.call.completed" -> {
                val delta = RunTelemetry(
                    runId = runId,
                    inputTokens = event.content["inputTokens"] as? Int ?: 0,
                    outputTokens = event.content["outputTokens"] as? Int ?: 0,
                    totalTokens = event.content["totalTokens"] as? Int ?: 0,
                )
                stats.merge(runId, delta) { existing, new ->
                    existing.copy(
                        inputTokens = existing.inputTokens + new.inputTokens,
                        outputTokens = existing.outputTokens + new.outputTokens,
                        totalTokens = existing.totalTokens + new.totalTokens,
                    )
                }
            }
            "tool.call" -> {
                stats.merge(runId, RunTelemetry(runId = runId, toolCallCount = 1)) { existing, _ ->
                    existing.copy(toolCallCount = existing.toolCallCount + 1)
                }
            }
        }
    }

    /** Returns accumulated stats for the given run id, or null if no events were received yet. */
    fun statsFor(runId: String): RunTelemetry? = stats[runId]

    /** Returns all accumulated run stats, ordered by insertion. */
    fun allStats(): List<RunTelemetry> = stats.values.toList()
}





