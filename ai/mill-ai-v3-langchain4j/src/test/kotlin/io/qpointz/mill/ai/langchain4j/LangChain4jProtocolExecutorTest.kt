package io.qpointz.mill.ai.langchain4j

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.AgentEvent
import io.qpointz.mill.ai.AgentProfile
import io.qpointz.mill.ai.ProtocolDefinition
import io.qpointz.mill.ai.ProtocolEventDefinition
import io.qpointz.mill.ai.ProtocolExecutionInput
import io.qpointz.mill.ai.ProtocolMode
import io.qpointz.mill.ai.RunState
import io.qpointz.mill.ai.ToolSchema
import io.qpointz.mill.ai.ToolSchemaField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LangChain4jProtocolExecutorTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun runState() = RunState(
        profile = AgentProfile(id = "test", capabilityIds = emptySet()),
        context = AgentContext(contextType = "general"),
    )

    /** Minimal non-empty message list required by ChatRequest. */
    private fun testMessages(): List<ChatMessage> = listOf(UserMessage.from("test input"))

    /** Fake model that streams the given tokens then completes with a synthetic response. */
    private fun streamingModel(vararg tokens: String): StreamingChatModel =
        object : StreamingChatModel {
            override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
                tokens.forEach { handler.onPartialResponse(it) }
                val fullText = tokens.joinToString("")
                handler.onCompleteResponse(
                    ChatResponse.builder()
                        .aiMessage(AiMessage.from(fullText))
                        .build()
                )
            }
        }

    /** Fake model that emits a single complete response with no streaming tokens. */
    private fun completingModel(responseText: String): StreamingChatModel =
        object : StreamingChatModel {
            override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
                handler.onCompleteResponse(
                    ChatResponse.builder()
                        .aiMessage(AiMessage.from(responseText))
                        .build()
                )
            }
        }

    private fun textProtocol(id: String = "conv.text") = ProtocolDefinition(
        id = id,
        description = "Text protocol.",
        mode = ProtocolMode.TEXT,
    )

    private fun structuredFinalProtocol(id: String = "chart.final") = ProtocolDefinition(
        id = id,
        description = "Chart spec.",
        mode = ProtocolMode.STRUCTURED_FINAL,
        finalSchema = ToolSchema.obj(
            properties = listOf(ToolSchemaField("title", ToolSchema.string())),
        ),
    )

    private fun structuredStreamProtocol(id: String = "table.stream") = ProtocolDefinition(
        id = id,
        description = "Table rows.",
        mode = ProtocolMode.STRUCTURED_STREAM,
        events = listOf(
            ProtocolEventDefinition("row", "A table row.", ToolSchema.obj()),
        ),
    )

    // ── TEXT mode ─────────────────────────────────────────────────────────────

    @Test
    fun `TEXT mode should emit ProtocolTextDelta for each streamed token`() {
        val model = streamingModel("Hello", " World")
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = textProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        val deltas = events.filterIsInstance<AgentEvent.ProtocolTextDelta>()
        assertEquals(2, deltas.size)
        assertEquals("Hello", deltas[0].text)
        assertEquals(" World", deltas[1].text)
    }

    @Test
    fun `TEXT mode should tag ProtocolTextDelta with protocol id`() {
        val model = streamingModel("Hi")
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = textProtocol("my.protocol"),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        val delta = events.filterIsInstance<AgentEvent.ProtocolTextDelta>().first()
        assertEquals("my.protocol", delta.protocolId)
    }

    @Test
    fun `TEXT mode should return accumulated text as result`() {
        val model = streamingModel("Hello", " World")
        val executor = LangChain4jProtocolExecutor(model)

        val result = executor.execute(ProtocolExecutionInput(
            protocol = textProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = {},
        ))

        assertEquals("Hello World", result.text)
    }

    @Test
    fun `TEXT mode should not emit ProtocolFinal or ProtocolStreamEvent`() {
        val model = streamingModel("Hi")
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = textProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolStreamEvent>().isEmpty())
    }

    // ── STRUCTURED_FINAL mode ─────────────────────────────────────────────────

    @Test
    fun `STRUCTURED_FINAL mode should emit one ProtocolFinal event`() {
        val payload = """{"title":"Sales"}"""
        val model = completingModel(payload)
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = structuredFinalProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        val finals = events.filterIsInstance<AgentEvent.ProtocolFinal>()
        assertEquals(1, finals.size)
        assertEquals(payload, finals[0].payload)
    }

    @Test
    fun `STRUCTURED_FINAL mode should tag ProtocolFinal with protocol id`() {
        val model = completingModel("""{"title":"X"}""")
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = structuredFinalProtocol("my.final"),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        assertEquals("my.final", events.filterIsInstance<AgentEvent.ProtocolFinal>().first().protocolId)
    }

    @Test
    fun `STRUCTURED_FINAL mode should return payload as result text`() {
        val payload = """{"title":"Revenue"}"""
        val model = completingModel(payload)
        val executor = LangChain4jProtocolExecutor(model)

        val result = executor.execute(ProtocolExecutionInput(
            protocol = structuredFinalProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = {},
        ))

        assertEquals(payload, result.text)
        assertEquals(payload, result.payload)
    }

    @Test
    fun `STRUCTURED_FINAL mode should not emit ProtocolTextDelta events`() {
        val model = completingModel("""{"title":"X"}""")
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = structuredFinalProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        assertTrue(events.filterIsInstance<AgentEvent.ProtocolTextDelta>().isEmpty())
    }

    // ── STRUCTURED_STREAM mode ────────────────────────────────────────────────

    @Test
    fun `STRUCTURED_STREAM mode should emit ProtocolStreamEvent per NDJSON line`() {
        val ndjson = """{"event":"row","content":{"id":1}}""" + "\n" +
                     """{"event":"row","content":{"id":2}}""" + "\n"
        val model = streamingModel(ndjson)
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = structuredStreamProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        val streamEvents = events.filterIsInstance<AgentEvent.ProtocolStreamEvent>()
        assertEquals(2, streamEvents.size)
    }

    @Test
    fun `STRUCTURED_STREAM mode should extract eventType from each NDJSON line`() {
        val ndjson = """{"event":"row","content":{"id":1}}""" + "\n"
        val model = streamingModel(ndjson)
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = structuredStreamProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        val streamEvent = events.filterIsInstance<AgentEvent.ProtocolStreamEvent>().first()
        assertEquals("row", streamEvent.eventType)
    }

    @Test
    fun `STRUCTURED_STREAM mode should tag events with protocol id`() {
        val ndjson = """{"event":"row","content":{}}""" + "\n"
        val model = streamingModel(ndjson)
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = structuredStreamProtocol("my.stream"),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        assertEquals("my.stream", events.filterIsInstance<AgentEvent.ProtocolStreamEvent>().first().protocolId)
    }

    @Test
    fun `STRUCTURED_STREAM mode should parse lines split across token chunks`() {
        // Line arrives in 3 chunks — the buffer must reassemble it before parsing
        val model = object : StreamingChatModel {
            override fun chat(request: ChatRequest, handler: StreamingChatResponseHandler) {
                handler.onPartialResponse("""{"event":"row",""")
                handler.onPartialResponse(""""content":{"id":1}}""")
                handler.onPartialResponse("\n")
                handler.onCompleteResponse(
                    ChatResponse.builder().aiMessage(AiMessage.from("")).build()
                )
            }
        }
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = structuredStreamProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        val streamEvents = events.filterIsInstance<AgentEvent.ProtocolStreamEvent>()
        assertEquals(1, streamEvents.size)
        assertEquals("row", streamEvents[0].eventType)
    }

    @Test
    fun `STRUCTURED_STREAM mode should not emit ProtocolFinal or ProtocolTextDelta`() {
        val ndjson = """{"event":"row","content":{}}""" + "\n"
        val model = streamingModel(ndjson)
        val executor = LangChain4jProtocolExecutor(model)
        val events = mutableListOf<AgentEvent>()

        executor.execute(ProtocolExecutionInput(
            protocol = structuredStreamProtocol(),
            runState = runState(),
            messages = testMessages(),
            listener = { events.add(it) },
        ))

        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolTextDelta>().isEmpty())
    }
}
