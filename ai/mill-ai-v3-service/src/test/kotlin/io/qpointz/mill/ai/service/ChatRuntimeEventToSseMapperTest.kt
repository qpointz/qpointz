package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.sse.ChatSseEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ChatRuntimeEventToSseMapperTest {

    private val mapper = ChatRuntimeEventToSseMapper(chatId = "chat-1")

    @Test
    fun shouldEmitToolEvents_withoutDuplicateItemCreated_whenChunksFollow() {
        val t1 = mapper.map(ChatRuntimeEvent.ToolCall("say_hello", mapOf("n" to 1), 0))
        assertEquals(2, t1.size)
        assertInstanceOf(ChatSseEvent.ItemCreated::class.java, t1[0])
        assertInstanceOf(ChatSseEvent.ItemToolCall::class.java, t1[1])
        val tool = t1[1] as ChatSseEvent.ItemToolCall
        assertEquals("say_hello", tool.toolName)
        assertEquals("item.tool.call", tool.type)

        val chunks = mapper.map(ChatRuntimeEvent.Chunk("hi"))
        assertEquals(1, chunks.size)
        assertInstanceOf(ChatSseEvent.ItemPartUpdated::class.java, chunks[0])

        val done = mapper.map(ChatRuntimeEvent.Completed("hi"))
        val completed = done.filterIsInstance<ChatSseEvent.ItemCompleted>().single()
        assertNull(completed.content)
    }

    @Test
    fun shouldEmitFullContent_onCompleted_whenOnlyToolEventsPrecededText() {
        mapper.map(ChatRuntimeEvent.ToolCall("t", emptyMap(), 0))
        mapper.map(ChatRuntimeEvent.ToolResult("t", "ok"))
        val events = mapper.map(ChatRuntimeEvent.Completed("final"))
        val completed = events.filterIsInstance<ChatSseEvent.ItemCompleted>().single()
        assertEquals("final", completed.content)
    }

    @Test
    fun shouldEmitItemDiagnostic_withSameItemId_asFollowingChunks() {
        val d = mapper.map(
            ChatRuntimeEvent.Diagnostic(
                code = "run.started",
                message = "Run started",
                detail = mapOf("profileId" to "hello-world"),
            ),
        )
        assertEquals(2, d.size)
        assertInstanceOf(ChatSseEvent.ItemCreated::class.java, d[0])
        assertInstanceOf(ChatSseEvent.ItemDiagnostic::class.java, d[1])
        val itemId = (d[0] as ChatSseEvent.ItemCreated).itemId
        val diag = d[1] as ChatSseEvent.ItemDiagnostic
        assertEquals(itemId, diag.itemId)
        assertEquals("run.started", diag.code)

        val chunk = mapper.map(ChatRuntimeEvent.Chunk("x"))
        assertEquals(itemId, (chunk[0] as ChatSseEvent.ItemPartUpdated).itemId)
    }

    @Test
    fun shouldShareItemId_acrossToolChunkAndCompleted() {
        val start = mapper.map(ChatRuntimeEvent.ToolCall("x", emptyMap(), 0))
        val itemId = (start[0] as ChatSseEvent.ItemCreated).itemId
        assertEquals(itemId, (start[1] as ChatSseEvent.ItemToolCall).itemId)
        mapper.map(ChatRuntimeEvent.Chunk("a"))
        val done = mapper.map(ChatRuntimeEvent.Completed("a"))
        assertEquals(itemId, (done[0] as ChatSseEvent.ItemCompleted).itemId)
    }
}
