package io.qpointz.mill.ai.test.scenario.v3

import io.qpointz.mill.ai.runtime.events.AgentEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TurnCheckRegistryTest {

    private val registry = TurnCheckRegistry.default()

    @Test
    fun shouldPassResponseContainsCheck() {
        val outcome = TurnOutcome(response = "Hello Bob from the tool", events = emptyList())
        val results = registry.runAll(
            outcome,
            VerifySpec(check = listOf(mapOf("response" to mapOf("contains" to "bob")))),
        )
        assertThat(results.single().result.passed).isTrue()
    }

    @Test
    fun shouldPassEventsContainsInOrderCheck() {
        val outcome = TurnOutcome(
            response = "ok",
            events = listOf(
                AgentEvent.RunStarted("hello-world"),
                AgentEvent.ToolCall("say_hello", mapOf("name" to "Alice"), 0),
                AgentEvent.AnswerCompleted("done"),
            ),
        )
        val results = registry.runAll(
            outcome,
            VerifySpec(
                check = listOf(
                    mapOf(
                        "events" to mapOf(
                            "containsInOrder" to listOf(
                                mapOf("type" to "run.started"),
                                mapOf("type" to "tool.call", "name" to "say_hello"),
                                mapOf("type" to "answer.completed"),
                            ),
                        ),
                    ),
                ),
            ),
        )
        assertThat(results).allMatch { it.result.passed }
    }

    @Test
    fun shouldFailResponseNotBlankCheck() {
        val outcome = TurnOutcome(response = "  ", events = emptyList())
        val results = registry.runAll(
            outcome,
            VerifySpec(check = listOf(mapOf("response" to mapOf("assert" to "not-blank")))),
        )
        assertThat(results.single().result.passed).isFalse()
    }
}
