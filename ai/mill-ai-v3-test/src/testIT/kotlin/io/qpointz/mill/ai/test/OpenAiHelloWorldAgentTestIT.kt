package io.qpointz.mill.ai.test

import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.AgentEvent
import io.qpointz.mill.ai.CapabilityRegistry
import io.qpointz.mill.ai.HelloWorldAgentProfile
import io.qpointz.mill.ai.langchain4j.OpenAiHelloWorldAgent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

/**
 * Real-model integration coverage for the hello-world validation agent.
 *
 * These tests prove both the direct-response path and the tool-calling path against a live
 * OpenAI-backed model.
 */
class OpenAiHelloWorldAgentTestIT {

    @Test
    fun `should support direct response path`() {
        val agent = requireAgent()
        val capabilities = helloWorldCapabilities()
        val events = mutableListOf<AgentEvent>()

        val response = agent.run(
            "Reply directly with a short greeting. Do not use tools.",
            events::add,
        )

        assertThat(response).isNotBlank()
        assertThat(capabilities.map { it.descriptor.id }).containsExactlyInAnyOrderElementsOf(HelloWorldAgentProfile.profile.capabilityIds)
        assertThat(events.map { it.type }).contains("run.started", "plan.created", "observation.made", "message.delta", "answer.completed")
        assertThat(events.map { it.type }).doesNotContain("tool.call")
        assertThat(events.filterIsInstance<AgentEvent.MessageDelta>()).isNotEmpty
        assertThat(events.first()).isEqualTo(AgentEvent.RunStarted(HelloWorldAgentProfile.profile.id))
        assertThat(events.filterIsInstance<AgentEvent.PlanCreated>().single().mode).isEqualTo("DIRECT_RESPONSE")
        assertThat(events.filterIsInstance<AgentEvent.ObservationMade>().single().decision).isEqualTo("FINISH")
        assertThat(events.map { it.type }.toSet()).isSubsetOf(protocolEventTypes(capabilities))
        assertThat(events.last()).isInstanceOf(AgentEvent.AnswerCompleted::class.java)
    }

    @Test
    fun `should support tool using path`() {
        val agent = requireAgent()
        val capabilities = helloWorldCapabilities()
        val events = mutableListOf<AgentEvent>()

        val response = agent.run(
            "Use the say_hello tool to greet Alice.",
            events::add,
        )

        assertThat(response).containsIgnoringCase("Alice")
        assertThat(events.map { it.type }).contains("plan.created", "tool.call", "tool.result", "observation.made", "answer.completed")
        assertThat(events.first().type).isEqualTo("run.started")
        assertThat(events.filterIsInstance<AgentEvent.PlanCreated>().single().mode).isEqualTo("CALL_TOOL")
        assertThat(events.filterIsInstance<AgentEvent.ToolCall>().single().name).isEqualTo("say_hello")
        assertThat(events.filterIsInstance<AgentEvent.ToolResult>().single().name).isEqualTo("say_hello")
        assertThat(events.filterIsInstance<AgentEvent.ObservationMade>().single().decision).isEqualTo("CONTINUE")
        assertThat(events.map { it.type }.toSet()).isSubsetOf(protocolEventTypes(capabilities))
    }

    private fun requireAgent(): OpenAiHelloWorldAgent {
        assumeTrue(System.getenv("OPENAI_API_KEY") != null, "OPENAI_API_KEY must be set for testIT")
        return requireNotNull(OpenAiHelloWorldAgent.fromEnv())
    }

    private fun helloWorldCapabilities() = CapabilityRegistry.load(javaClass.classLoader)
        .capabilitiesFor(HelloWorldAgentProfile.profile, AgentContext(contextType = "general"))

    private fun protocolEventTypes(capabilities: List<io.qpointz.mill.ai.Capability>): Set<String> =
        capabilities.flatMap { capability -> capability.protocols.flatMap { it.eventTypes } }.toSet()
}
