package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.test.scenario.v3.ScriptStep
import io.qpointz.mill.ai.test.scenario.v3.ScriptToolCall
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome
import org.junit.jupiter.api.Test

/**
 * Smoke coverage for activity logging helpers (no log capture; ensures wiring compiles and runs).
 */
class ScenarioActivityLoggerTest {

    private val ctx = ScenarioActivityLogger.TurnContext(
        packName = "test-pack",
        profileId = "hello-world",
        mode = "scripted",
        turnIndex = 0,
        runnerKind = "scripted",
        conversationId = "chat-1",
    )

    @Test
    fun shouldEmitActivityLogsWithoutThrowing() {
        ScenarioActivityLogger.logPackStarted("test-pack", "hello-world", "scripted", 1, "test.yml")
        ScenarioActivityLogger.logTurnStarted(ctx, "Say hello")
        ScenarioActivityLogger.logScriptModelStep(
            ctx,
            invocation = 0,
            step = ScriptStep(toolCalls = listOf(ScriptToolCall(name = "say_hello", args = mapOf("name" to "Bob")))),
        )
        ScenarioActivityLogger.logAgentEvent(ctx, AgentEvent.ToolCall("say_hello", mapOf("name" to "Bob"), 0))
        ScenarioActivityLogger.logAgentEvent(ctx, AgentEvent.AnswerCompleted("Hello Bob"))
        ScenarioActivityLogger.logTurnCompleted(
            ctx,
            TurnOutcome(
                response = "Hello Bob",
                events = listOf(
                    AgentEvent.RunStarted("hello-world"),
                    AgentEvent.AnswerCompleted("Hello Bob"),
                ),
            ),
        )
        ScenarioActivityLogger.logPackFinished("test-pack", "PASS", 12L, emptyList())
    }
}
