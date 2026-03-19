package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Verifies Lane 3 memory behavior for [LangChain4jAgent] without a real LLM.
 *
 * Uses an empty-capability profile so [CapabilityRegistry.from] can return an
 * empty list that satisfies the profile's capability-set requirement check.
 */
class LangChain4jAgentMemoryTest {

    private val emptyProfile = AgentProfile(id = "test", capabilityIds = emptySet())
    private val emptyRegistry = CapabilityRegistry.from(emptyList())

    /** Mock model that always responds with [responseText] and no tool calls. */
    private class FixedResponseModel(private val responseText: String) : StreamingChatModel {
        override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
            val response = ChatResponse.builder()
                .aiMessage(AiMessage.from(responseText))
                .build()
            handler.onCompleteResponse(response)
        }
    }

    private fun agent(
        responseText: String,
        store: InMemoryChatMemoryStore,
        maxMessages: Int = 40,
    ) = LangChain4jAgent(
        model = FixedResponseModel(responseText),
        profile = emptyProfile,
        registry = emptyRegistry,
        chatMemoryStore = store,
        memoryStrategy = BoundedWindowMemoryStrategy(maxMessages),
    )

    @Test
    fun `shouldSaveMemory_afterFirstTurn`() {
        val store = InMemoryChatMemoryStore()
        val session = ConversationSession(profileId = "test")
        agent("Hi there!", store).run("Hello", session)

        val memory = store.load(session.conversationId)
        assertNotNull(memory)
        assertTrue(memory!!.messages.any { it.role == MessageRole.USER && it.content == "Hello" })
        assertTrue(memory.messages.any { it.role == MessageRole.ASSISTANT && it.content == "Hi there!" })
    }

    @Test
    fun `shouldAccumulateMemory_acrossMultipleTurns`() {
        val store = InMemoryChatMemoryStore()
        val session = ConversationSession(profileId = "test")
        val a = agent("reply", store)

        a.run("turn1", session)
        a.run("turn2", session)
        a.run("turn3", session)

        val memory = store.load(session.conversationId)!!
        // 3 turns × (user + assistant) = 6 messages
        assertEquals(6, memory.messages.size)
        assertEquals(listOf("turn1", "turn2", "turn3"), memory.messages.filter { it.role == MessageRole.USER }.map { it.content })
    }

    @Test
    fun `shouldProjectBoundedHistory_onSubsequentTurn`() {
        val store = InMemoryChatMemoryStore()
        val session = ConversationSession(profileId = "test")
        // Window of 2 means only 1 prior pair (user+assistant) reaches the model
        val a = agent("reply", store, maxMessages = 2)

        a.run("q1", session)
        a.run("q2", session)
        a.run("q3", session)

        // Store should have all 3 turns regardless of projection
        val memory = store.load(session.conversationId)!!
        assertEquals(6, memory.messages.size)
    }

    @Test
    fun `shouldYieldFreshContext_forNewConversationId`() {
        val store = InMemoryChatMemoryStore()
        val session1 = ConversationSession(profileId = "test")
        val session2 = ConversationSession(profileId = "test")
        val a = agent("reply", store)

        a.run("from session1", session1)
        a.run("from session2", session2)

        // Each session has its own memory; they do not share state
        val m1 = store.load(session1.conversationId)!!
        val m2 = store.load(session2.conversationId)!!
        assertEquals(1, m1.messages.filter { it.role == MessageRole.USER }.size)
        assertEquals(1, m2.messages.filter { it.role == MessageRole.USER }.size)
        assertEquals("from session1", m1.messages.first { it.role == MessageRole.USER }.content)
        assertEquals("from session2", m2.messages.first { it.role == MessageRole.USER }.content)
    }

    @Test
    fun `shouldClearMemory_whenStoreCleared`() {
        val store = InMemoryChatMemoryStore()
        val session = ConversationSession(profileId = "test")
        val a = agent("reply", store)

        a.run("before clear", session)
        assertNotNull(store.load(session.conversationId))

        store.clear(session.conversationId)
        assertNull(store.load(session.conversationId))

        // After clearing the store, a fresh run should start from empty memory
        a.run("after clear", session)
        val memory = store.load(session.conversationId)!!
        assertEquals(1, memory.messages.filter { it.role == MessageRole.USER }.size)
        assertEquals("after clear", memory.messages.first { it.role == MessageRole.USER }.content)
    }

    @Test
    fun `shouldUseInjectedStore_notHardcodedState`() {
        val store1 = InMemoryChatMemoryStore()
        val store2 = InMemoryChatMemoryStore()
        val session = ConversationSession(profileId = "test")

        agent("reply", store1).run("stored in store1", session)

        // store2 has no knowledge of this turn
        assertNull(store2.load(session.conversationId))
        assertNotNull(store1.load(session.conversationId))
    }
}





