package io.qpointz.mill.ai.test.scenario.json

import tools.jackson.databind.ObjectMapper
import io.qpointz.mill.ai.test.AgentScenarioResult
import io.qpointz.mill.ai.test.scenario.Expectations

private val mapper = ObjectMapper()

/**
 * Parses [AgentScenarioResult.response] as a single JSON object and runs
 * [JsonNodeAssert] assertions against it.
 */
data class JsonExpectations(val asserts: List<JsonNodeAssert>) : Expectations {
    override fun assert(result: AgentScenarioResult) {
        val node = mapper.readTree(result.response)
        asserts.forEach { it.assert(node) }
    }
}

/**
 * Parses [AgentScenarioResult.response] as JSONL (one JSON object per line)
 * and runs [JsonListAssert] assertions against the resulting list.
 */
data class JsonListExpectations(val asserts: List<JsonListAssert>) : Expectations {
    override fun assert(result: AgentScenarioResult) {
        val nodes = result.response
            .lines()
            .filter { it.isNotBlank() }
            .map { mapper.readTree(it) }
        asserts.forEach { it.assert(nodes) }
    }
}
