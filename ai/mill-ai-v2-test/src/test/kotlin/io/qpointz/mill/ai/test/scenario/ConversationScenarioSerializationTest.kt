package io.qpointz.mill.ai.test.scenario

import io.qpointz.mill.ai.test.scenario.json.JsonListAssert
import io.qpointz.mill.ai.test.scenario.json.JsonNodeListExpectations
import io.qpointz.mill.ai.test.scenario.json.JsonListJsonPathAssert
import io.qpointz.mill.ai.test.scenario.json.JsonListNonEmptyAssert
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConversationScenarioSerializationTest {

    @Test
    fun `read-trivial`() {
        val cs = ConversationScenario.fromResource("ai-test/test/scenario/trivial.yml")
        assertNotNull(cs)
    }

    @Test
    fun `read-json-expects`() {
        val cs = ConversationScenario.fromResource("ai-test/test/scenario/json.expects.yml")
        assertNotNull(cs)
        val scenario = cs.get(0);
        val expectations = scenario.expect

        assertTrue { expectations is JsonNodeListExpectations }
        val jsonList = expectations as JsonNodeListExpectations

        assertTrue { jsonList.asserts.get(0) is JsonListNonEmptyAssert }
        assertTrue { jsonList.asserts.get(1) is JsonListJsonPathAssert }



    }


}