package io.qpointz.mill.ai.test

import io.qpointz.mill.ai.AgentEvent
import io.qpointz.mill.ai.ConversationSession
import io.qpointz.mill.ai.capabilities.HelloWorldAgentProfile
import io.qpointz.mill.ai.langchain4j.LangChain4jAgent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

/**
 * Real-model integration coverage for the hello-world validation agent.
 *
 * These tests prove both the direct-response path and the tool-calling path against a live
 * OpenAI-backed model.
 */
class LangChain4jAgentHelloWorldTestIT {

    @Test
    fun `should support direct response path`() {
        val agent = requireAgent()
        val events = mutableListOf<AgentEvent>()

        val response = agent.run(
            "Reply directly with a short greeting. Do not use tools.",
            ConversationSession(),
            events::add,
        )

        assertThat(response).isNotBlank()
        assertThat(events.map { it.type }).contains("run.started", "answer.completed")
        assertThat(events.map { it.type }).doesNotContain("tool.call")
        assertThat(events.first()).isEqualTo(AgentEvent.RunStarted(HelloWorldAgentProfile.profile.id))
        assertThat(events.last()).isInstanceOf(AgentEvent.AnswerCompleted::class.java)
    }

    @Test
    fun `should support tool using path`() {
        val agent = requireAgent()
        val events = mutableListOf<AgentEvent>()

        val response = agent.run(
            "Use the say_hello tool to greet Alice.",
            ConversationSession(),
            events::add,
        )

        assertThat(response).containsIgnoringCase("Alice")
        assertThat(events.map { it.type }).contains("tool.call", "tool.result", "answer.completed")
        assertThat(events.first().type).isEqualTo("run.started")
        assertThat(events.filterIsInstance<AgentEvent.ToolCall>().map { it.name }).contains("say_hello")
        assertThat(events.filterIsInstance<AgentEvent.ToolResult>().map { it.name }).contains("say_hello")
    }

    private fun requireAgent(): LangChain4jAgent {
        assumeTrue(System.getenv("OPENAI_API_KEY") != null, "OPENAI_API_KEY must be set for testIT")
        return requireNotNull(LangChain4jAgent.fromEnv(HelloWorldAgentProfile.profile))
    }
}
