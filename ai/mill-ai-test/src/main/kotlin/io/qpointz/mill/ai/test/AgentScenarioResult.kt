package io.qpointz.mill.ai.test

import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome

/**
 * Legacy-compatible result wrapper; prefer [TurnOutcome] for v3 scenarios.
 *
 * @param response Final assistant text.
 * @param events Runtime events for the turn.
 */
data class AgentScenarioResult(
    val response: String,
    val events: List<AgentEvent>,
) {
    /** Converts to v3 [TurnOutcome] without artefact/SSE slices. */
    fun toTurnOutcome(): TurnOutcome = TurnOutcome(response = response, events = events)
}
