package io.qpointz.mill.ai.test.scenario

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConversationScenarioSerializationTest {

    @Test
    fun `read-trivial`() {
        val cs = ConversationScenario.fromResource("ai-test/test/scenario/trivial.yml")
        assertNotNull(cs)
    }


}