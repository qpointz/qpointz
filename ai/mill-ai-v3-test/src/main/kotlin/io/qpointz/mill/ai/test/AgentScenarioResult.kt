package io.qpointz.mill.ai.test

import io.qpointz.mill.ai.runtime.events.AgentEvent

data class AgentScenarioResult(
    val response: String,
    val events: List<AgentEvent>,
)
