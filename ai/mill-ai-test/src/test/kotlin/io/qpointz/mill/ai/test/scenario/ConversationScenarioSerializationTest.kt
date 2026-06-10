package io.qpointz.mill.ai.test.scenario

import io.qpointz.mill.ai.test.scenario.json.JsonListExpectations
import io.qpointz.mill.ai.test.scenario.json.JsonListJsonPathAssert
import io.qpointz.mill.ai.test.scenario.json.JsonListNonEmptyAssert
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConversationScenarioSerializationTest {

    @Test
    fun `shouldLoad_trivialScenario`() {
        val scenarios = ConversationScenario.fromResource("ai-test/scenario/trivial.yml")
        assertEquals(1, scenarios.size)
        assertEquals("trivial scenario", scenarios[0].name)
        assertEquals(2, scenarios[0].steps.size)
        assertEquals("say hello", scenarios[0].steps[0].user)
        assertNull(scenarios[0].steps[0].expect)
    }

    @Test
    fun `shouldDeserialise_jsonListExpectations`() {
        val scenarios = ConversationScenario.fromResource("ai-test/scenario/json-expects.yml")
        assertEquals(1, scenarios.size)
        val step = scenarios[0].steps[0]
        val expect = step.expect
        assertTrue(expect is JsonListExpectations, "Expected JsonListExpectations, got ${expect?.javaClass}")
        val jsonList = expect as JsonListExpectations
        assertEquals(2, jsonList.asserts.size)
        assertTrue(jsonList.asserts[0] is JsonListNonEmptyAssert)
        assertTrue(jsonList.asserts[1] is JsonListJsonPathAssert)
        assertEquals("\$[?(@.name)]", (jsonList.asserts[1] as JsonListJsonPathAssert).exp)
    }
}
