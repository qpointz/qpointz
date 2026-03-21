package io.qpointz.mill.ai.sse

import io.qpointz.mill.ai.runtime.events.AgentEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChatSseEventTypesTest {

    @Test
    fun shouldHaveCorrectType_itemCreated() {
        val e = ChatSseEvent.ItemCreated("eid", "cid", "iid", 0, java.time.Instant.now())
        assertEquals("item.created", e.type)
    }

    @Test
    fun shouldHaveCorrectType_itemPartUpdated() {
        val e = ChatSseEvent.ItemPartUpdated("eid", "cid", "iid", 1, java.time.Instant.now(), content = "hello")
        assertEquals("item.part.updated", e.type)
    }

    @Test
    fun shouldHaveCorrectType_itemCompleted() {
        val e = ChatSseEvent.ItemCompleted("eid", "cid", "iid", 2, java.time.Instant.now(), content = "hello")
        assertEquals("item.completed", e.type)
    }

    @Test
    fun shouldHaveCorrectType_itemFailed() {
        val e = ChatSseEvent.ItemFailed("eid", "cid", "iid", 3, java.time.Instant.now(), code = "agent.error", reason = "oops")
        assertEquals("item.failed", e.type)
    }

    @Test
    fun shouldHaveV1FrozenDefaults_itemPartUpdated() {
        val e = ChatSseEvent.ItemPartUpdated("eid", "cid", "iid", 0, java.time.Instant.now(), content = "x")
        assertEquals("conversation", e.presentation)
        assertEquals("text", e.partType)
        assertEquals("append", e.mode)
    }

    @Test
    fun shouldHaveV1FrozenDefaults_itemCompleted() {
        val e = ChatSseEvent.ItemCompleted("eid", "cid", "iid", 0, java.time.Instant.now(), content = "x")
        assertEquals("conversation", e.presentation)
        assertEquals("text", e.partType)
    }

    @Test
    fun shouldHaveCodeAndReason_itemFailed() {
        val e = ChatSseEvent.ItemFailed("eid", "cid", "iid", 3, java.time.Instant.now(), code = "runtime.unavailable", reason = "LLM offline")
        assertEquals("runtime.unavailable", e.code)
        assertEquals("LLM offline", e.reason)
    }
}

class AgentEventToSseMapperTest {

    private val mapper = AgentEventToSseMapper(chatId = "chat-1")

    @Test
    fun shouldEmitItemCreatedAndPartUpdated_onFirstMessageDelta() {
        val events = mapper.map(AgentEvent.MessageDelta("hello"))
        assertEquals(2, events.size)
        assertInstanceOf(ChatSseEvent.ItemCreated::class.java, events[0])
        assertInstanceOf(ChatSseEvent.ItemPartUpdated::class.java, events[1])
    }

    @Test
    fun shouldNotEmitItemCreated_onSubsequentMessageDelta() {
        mapper.map(AgentEvent.MessageDelta("first"))
        val events = mapper.map(AgentEvent.MessageDelta("second"))
        assertEquals(1, events.size)
        assertInstanceOf(ChatSseEvent.ItemPartUpdated::class.java, events[0])
    }

    @Test
    fun shouldPreserveContent_inPartUpdated() {
        mapper.map(AgentEvent.MessageDelta("x")) // start item
        val events = mapper.map(AgentEvent.MessageDelta("hello world"))
        val part = events[0] as ChatSseEvent.ItemPartUpdated
        assertEquals("hello world", part.content)
    }

    @Test
    fun shouldShareItemId_acrossDeltasAndCompleted() {
        val created = (mapper.map(AgentEvent.MessageDelta("a"))[0] as ChatSseEvent.ItemCreated).itemId
        val part = (mapper.map(AgentEvent.MessageDelta("b"))[0] as ChatSseEvent.ItemPartUpdated).itemId
        val completed = (mapper.map(AgentEvent.AnswerCompleted("ab"))[0] as ChatSseEvent.ItemCompleted).itemId
        assertEquals(created, part)
        assertEquals(created, completed)
    }

    @Test
    fun shouldEmitItemCompleted_onAnswerCompleted() {
        mapper.map(AgentEvent.MessageDelta("x"))
        val events = mapper.map(AgentEvent.AnswerCompleted("final answer"))
        val completed = events.filterIsInstance<ChatSseEvent.ItemCompleted>()
        assertEquals(1, completed.size)
        // content is null because deltas were emitted — streaming consumers use accumulated text
        assertNull(completed[0].content)
    }

    @Test
    fun shouldCarryFullContent_whenNoDeltas() {
        // non-streaming path: AnswerCompleted without any prior MessageDelta
        val events = mapper.map(AgentEvent.AnswerCompleted("direct answer"))
        val completed = events.filterIsInstance<ChatSseEvent.ItemCompleted>().single()
        assertEquals("direct answer", completed.content)
    }

    @Test
    fun shouldEmitItemCreatedAndCompleted_whenNoDeltas() {
        val events = mapper.map(AgentEvent.AnswerCompleted("direct answer"))
        assertEquals(2, events.size)
        assertInstanceOf(ChatSseEvent.ItemCreated::class.java, events[0])
        assertInstanceOf(ChatSseEvent.ItemCompleted::class.java, events[1])
    }

    @Test
    fun shouldAssignNewItemId_afterCompletion() {
        val firstCreated = (mapper.map(AgentEvent.MessageDelta("a"))[0] as ChatSseEvent.ItemCreated).itemId
        mapper.map(AgentEvent.AnswerCompleted("done"))
        val secondCreated = (mapper.map(AgentEvent.MessageDelta("b"))[0] as ChatSseEvent.ItemCreated).itemId
        assertNotEquals(firstCreated, secondCreated)
    }

    @Test
    fun shouldIncrementSequence_acrossEvents() {
        mapper.map(AgentEvent.MessageDelta("a"))
        val delta = mapper.map(AgentEvent.MessageDelta("b"))[0]
        val completed = mapper.map(AgentEvent.AnswerCompleted("c"))[0]
        assertTrue(delta.sequence < completed.sequence)
    }

    @Test
    fun shouldIgnoreUnmappedEvents() {
        assertEquals(emptyList<ChatSseEvent>(), mapper.map(AgentEvent.RunStarted("profile-a")))
        assertEquals(emptyList<ChatSseEvent>(), mapper.map(AgentEvent.ToolCall("tool", emptyMap(), 0)))
        assertEquals(emptyList<ChatSseEvent>(), mapper.map(AgentEvent.LlmCallCompleted(10, 20, 30)))
    }

    @Test
    fun shouldProduceItemFailed_withCorrectChatId() {
        val failed = mapper.fail("agent.error", "something went wrong")
        assertEquals("chat-1", failed.chatId)
        assertEquals("item.failed", failed.type)
        assertEquals("agent.error", failed.code)
        assertEquals("something went wrong", failed.reason)
    }

    @Test
    fun shouldSetChatId_onAllEvents() {
        val events = mapper.map(AgentEvent.MessageDelta("x"))
        events.forEach { assertEquals("chat-1", it.chatId) }
    }
}
