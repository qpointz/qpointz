package io.qpointz.mill.ai.runtime.langchain4j

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import io.qpointz.mill.ai.capabilities.DemoCapabilityProvider
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.ConversationSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class LangChain4jAgentMaxIterationsTest {

    private val profile = AgentProfile(id = "test-loop", capabilityIds = setOf("demo"))
    private val registry = CapabilityRegistry.from(listOf(DemoCapabilityProvider()))

    /** Always requests `say_hello` so the native tool loop never reaches a final answer. */
    private class ToolLoopModel : StreamingChatModel {
        override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
            val aiMessage = AiMessage.from(
                listOf(
                    ToolExecutionRequest.builder()
                        .id("loop")
                        .name("say_hello")
                        .arguments("""{"name":"loop"}""")
                        .build(),
                ),
            )
            handler.onCompleteResponse(ChatResponse.builder().aiMessage(aiMessage).build())
        }
    }

    @Test
    fun shouldRejectNonPositiveMaxIterations() {
        assertThrows(IllegalArgumentException::class.java) {
            LangChain4jAgent.validateMaxIterations(0)
        }
    }

    @Test
    fun shouldStopAfterConfiguredMaxIterations() {
        val agent = LangChain4jAgent(
            model = ToolLoopModel(),
            profile = profile,
            registry = registry,
            maxIterations = 1,
        )
        val answer = agent.run(
            input = "loop",
            session = ConversationSession(profileId = profile.id),
            context = AgentContext(contextType = "general"),
        )
        assertEquals(LangChain4jAgent.MAX_ITERATIONS_FALLBACK, answer)
    }
}
