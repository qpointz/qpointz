package io.qpointz.mill.ai.test.scenario.text

import io.qpointz.mill.ai.test.AgentScenarioResult
import io.qpointz.mill.ai.test.scenario.Expectations

data class TextExpectations(val asserts: List<TextAssert>) : Expectations {
    override fun assert(result: AgentScenarioResult) {
        asserts.forEach { it.assert(result.response) }
    }
}
