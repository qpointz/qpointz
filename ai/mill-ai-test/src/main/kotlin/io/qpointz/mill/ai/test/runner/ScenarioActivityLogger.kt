package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.test.scenario.v3.ArtifactSnapshot
import io.qpointz.mill.ai.test.scenario.v3.ScriptStep
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome
import org.slf4j.LoggerFactory

/**
 * Structured activity logging for scenario harness runs (scripted and live).
 *
 * Log level guidance:
 * - **INFO** — turn boundaries, tool calls/results, plans, protocols, final answers
 * - **DEBUG** — streaming token deltas (message, reasoning, thinking)
 */
internal object ScenarioActivityLogger {

    private val log = LoggerFactory.getLogger("io.qpointz.mill.ai.test.scenario")

    /**
     * Context for a single scenario turn log stream.
     *
     * @param packName YAML pack name.
     * @param profileId Agent profile id.
     * @param mode Pack `parameters.mode` (`scripted` or `live`).
     * @param turnIndex Zero-based turn index.
     * @param runnerKind Runner implementation label (`scripted` or `provided`).
     * @param conversationId Session conversation id.
     */
    data class TurnContext(
        val packName: String,
        val profileId: String,
        val mode: String,
        val turnIndex: Int,
        val runnerKind: String,
        val conversationId: String,
    )

    /**
     * Logs the start of a scenario pack run.
     */
    fun logPackStarted(packName: String, profileId: String, mode: String, turnCount: Int, source: String) {
        log.info("scenario pack started name={} profile={} mode={} turns={} source={}", packName, profileId, mode, turnCount, source)
    }

    /**
     * Logs the end of a scenario pack run.
     */
    fun logPackFinished(packName: String, overall: String, durationMs: Long, failures: List<String>) {
        if (failures.isEmpty()) {
            log.info("scenario pack finished name={} overall={} durationMs={}", packName, overall, durationMs)
        } else {
            log.warn(
                "scenario pack finished name={} overall={} durationMs={} failures={}",
                packName,
                overall,
                durationMs,
                failures,
            )
        }
    }

    /**
     * Logs user input before the agent runs.
     */
    fun logTurnStarted(ctx: TurnContext, ask: String) {
        log.info("{} ask={}", ctx.prefix(), truncate(ask))
    }

    /**
     * Logs a runtime [AgentEvent] as the turn progresses.
     */
    fun logAgentEvent(ctx: TurnContext, event: AgentEvent) {
        when (event) {
            is AgentEvent.MessageDelta,
            is AgentEvent.ReasoningDelta,
            is AgentEvent.ThinkingDelta,
            is AgentEvent.ProtocolTextDelta,
            -> log.debug("{} event {}", ctx.prefix(), formatEvent(event))

            else -> log.info("{} event {}", ctx.prefix(), formatEvent(event))
        }
    }

    /**
     * Logs a scripted model queue step (one LLM invocation).
     */
    fun logScriptModelStep(ctx: TurnContext, invocation: Int, step: ScriptStep) {
        val toolCalls = step.toolCalls
        val answer = step.answer
        val detail = when {
            toolCalls != null ->
                "toolCalls=${toolCalls.map { it.name }}"
            answer != null ->
                "answer=${truncate(answer)}"
            else -> "empty step"
        }
        log.info("{} scripted-model invocation={} {}", ctx.prefix(), invocation, detail)
    }

    /**
     * Logs turn completion with response and collected artefacts.
     */
    fun logTurnCompleted(ctx: TurnContext, outcome: TurnOutcome) {
        log.info(
            "{} completed response={} events={} artifacts={} sse={}",
            ctx.prefix(),
            truncate(outcome.response),
            outcome.events.size,
            formatArtifacts(outcome.artifacts),
            outcome.sseEvents.size,
        )
        if (log.isDebugEnabled) {
            outcome.events.forEachIndexed { index, event ->
                log.debug("{} event[{}] {}", ctx.prefix(), index, formatEvent(event))
            }
        }
    }

    private fun TurnContext.prefix(): String =
        "[$packName turn=$turnIndex mode=$mode runner=$runnerKind chat=$conversationId]"

    private fun formatEvent(event: AgentEvent): String =
        when (event) {
            is AgentEvent.RunStarted -> "run.started profileId=${event.profileId}"
            is AgentEvent.ThinkingDelta -> "thinking.delta message=${truncate(event.message)}"
            is AgentEvent.PlanCreated -> "plan.created mode=${event.mode} tool=${event.toolName}"
            is AgentEvent.MessageDelta -> "message.delta text=${truncate(event.text)}"
            is AgentEvent.ToolCall ->
                "tool.call name=${event.name} iteration=${event.iteration} args=${summarize(event.arguments)}"
            is AgentEvent.ToolResult ->
                "tool.result name=${event.name} result=${summarize(event.result)}"
            is AgentEvent.ObservationMade ->
                "observation.made decision=${event.decision} reason=${truncate(event.reason)}"
            is AgentEvent.AnswerCompleted -> "answer.completed text=${truncate(event.text)}"
            is AgentEvent.ReasoningDelta -> "reasoning.delta text=${truncate(event.text)}"
            is AgentEvent.ProtocolTextDelta ->
                "protocol.text.delta protocolId=${event.protocolId} text=${truncate(event.text)}"
            is AgentEvent.ProtocolFinal ->
                "protocol.final protocolId=${event.protocolId} payload=${summarize(event.payload)}"
            is AgentEvent.ProtocolStreamEvent ->
                "protocol.stream.event protocolId=${event.protocolId} type=${event.eventType} payload=${summarize(event.payload)}"
            is AgentEvent.LlmCallCompleted ->
                "llm.call.completed input=${event.inputTokens} output=${event.outputTokens} total=${event.totalTokens}"
        }

    private fun formatArtifacts(artifacts: List<ArtifactSnapshot>): String =
        if (artifacts.isEmpty()) {
            "none"
        } else {
            artifacts.joinToString(prefix = "[", postfix = "]") { "${it.persistKind}:${it.artifactId}" }
        }

    private fun summarize(value: Any?): String =
        when (value) {
            null -> "null"
            is Map<*, *> -> truncate(value.toString())
            is String -> truncate(value)
            else -> truncate(value.toString())
        }

    private fun truncate(text: String, max: Int = 240): String =
        if (text.length <= max) text else text.take(max) + "…(${text.length} chars)"
}
