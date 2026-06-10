package io.qpointz.mill.ai.memory

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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BoundedWindowMemoryStrategyTest {

    private fun msg(role: MessageRole, content: String) = ConversationMessage(role, content)

    private fun input(messages: List<ConversationMessage>?, id: String = "c1") = MemoryProjectionInput(
        conversationId = id,
        profileId = "test",
        memory = messages?.let { ConversationMemory(id, "test", it) },
    )

    @Test
    fun `shouldReturnEmpty_whenMemoryIsNull`() {
        val result = BoundedWindowMemoryStrategy().project(input(null))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `shouldReturnEmpty_whenMemoryHasNoMessages`() {
        val result = BoundedWindowMemoryStrategy().project(input(emptyList()))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `shouldReturnAll_whenUnderWindowSize`() {
        val messages = listOf(msg(MessageRole.USER, "a"), msg(MessageRole.ASSISTANT, "b"))
        val result = BoundedWindowMemoryStrategy(maxMessages = 10).project(input(messages))
        assertEquals(messages, result)
    }

    @Test
    fun `shouldReturnAll_whenExactlyWindowSize`() {
        val messages = (1..4).map { msg(MessageRole.USER, "m$it") }
        val result = BoundedWindowMemoryStrategy(maxMessages = 4).project(input(messages))
        assertEquals(messages, result)
    }

    @Test
    fun `shouldReturnLastN_whenOverWindowSize`() {
        val messages = (1..10).map { msg(MessageRole.USER, "m$it") }
        val result = BoundedWindowMemoryStrategy(maxMessages = 4).project(input(messages))
        assertEquals(4, result.size)
        assertEquals("m7", result[0].content)
        assertEquals("m10", result[3].content)
    }

    @Test
    fun `shouldPreserveMessageOrder_afterTruncation`() {
        val messages = listOf(
            msg(MessageRole.USER, "q1"), msg(MessageRole.ASSISTANT, "a1"),
            msg(MessageRole.USER, "q2"), msg(MessageRole.ASSISTANT, "a2"),
            msg(MessageRole.USER, "q3"), msg(MessageRole.ASSISTANT, "a3"),
        )
        val result = BoundedWindowMemoryStrategy(maxMessages = 4).project(input(messages))
        assertEquals(listOf("q2", "a2", "q3", "a3"), result.map { it.content })
    }
}





