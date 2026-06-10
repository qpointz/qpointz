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

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType
import dev.langchain4j.model.chat.request.json.JsonSchema
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import java.util.concurrent.CompletableFuture

/**
 * LangChain4j-backed implementation of ProtocolExecutor.
 *
 * Supports all three protocol modes:
 * - TEXT: streams tokens, emits ProtocolTextDelta per token
 * - STRUCTURED_FINAL: collects full JSON response, validates, emits ProtocolFinal
 * - STRUCTURED_STREAM: parses NDJSON lines from streamed text, emits ProtocolStreamEvent per line
 */
class LangChain4jProtocolExecutor(
    private val model: StreamingChatModel,
    private val objectMapper: JsonMapper = JsonMapper.builder().build(),
) : ProtocolExecutor {

    override fun execute(input: ProtocolExecutionInput): ProtocolExecutionResult {
        @Suppress("UNCHECKED_CAST")
        val messages = input.messages as List<ChatMessage>
        return when (input.protocol.mode) {
            ProtocolMode.TEXT -> executeText(input, messages)
            ProtocolMode.STRUCTURED_FINAL -> executeStructuredFinal(input, messages)
            ProtocolMode.STRUCTURED_STREAM -> executeStructuredStream(input, messages)
        }
    }

    private fun executeText(
        input: ProtocolExecutionInput,
        messages: List<ChatMessage>,
    ): ProtocolExecutionResult {
        val future = CompletableFuture<String>()
        val accumulated = StringBuilder()

        model.chat(
            ChatRequest.builder().messages(messages).build(),
            object : StreamingChatResponseHandler {
                override fun onPartialResponse(partialResponse: String) {
                    accumulated.append(partialResponse)
                    input.listener(AgentEvent.ProtocolTextDelta(protocolId = input.protocol.id, text = partialResponse))
                }

                override fun onCompleteResponse(completeResponse: dev.langchain4j.model.chat.response.ChatResponse) {
                    future.complete(completeResponse.aiMessage().text() ?: accumulated.toString())
                }

                override fun onError(error: Throwable) {
                    future.completeExceptionally(error)
                }
            }
        )

        val text = future.join()
        return ProtocolExecutionResult(text = text)
    }

    private fun executeStructuredFinal(
        input: ProtocolExecutionInput,
        messages: List<ChatMessage>,
    ): ProtocolExecutionResult {
        val finalSchema = requireNotNull(input.protocol.finalSchema) {
            "STRUCTURED_FINAL protocol '${input.protocol.id}' must declare finalSchema."
        }

        val jsonSchema = JsonSchema.builder()
            .name(input.protocol.id.replace(".", "_"))
            .rootElement(finalSchema)
            .build()

        val future = CompletableFuture<String>()

        model.chat(
            ChatRequest.builder()
                .messages(messages)
                .responseFormat(
                    ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(jsonSchema)
                        .build()
                )
                .build(),
            object : StreamingChatResponseHandler {
                override fun onPartialResponse(partialResponse: String) = Unit

                override fun onCompleteResponse(completeResponse: dev.langchain4j.model.chat.response.ChatResponse) {
                    future.complete(completeResponse.aiMessage().text().orEmpty())
                }

                override fun onError(error: Throwable) {
                    future.completeExceptionally(error)
                }
            }
        )

        val rawPayload = future.join()
        val parsedPayload: Any? = objectMapper.readValue(rawPayload, object : TypeReference<Any?>() {})
        input.listener(AgentEvent.ProtocolFinal(protocolId = input.protocol.id, payload = parsedPayload))
        return ProtocolExecutionResult(text = rawPayload, payload = rawPayload)
    }

    private fun executeStructuredStream(
        input: ProtocolExecutionInput,
        messages: List<ChatMessage>,
    ): ProtocolExecutionResult {
        val future = CompletableFuture<Unit>()
        val emittedPayloads = mutableListOf<String>()

        val buffer = JsonlLineBuffer(
            onLine = { line ->
                val node = objectMapper.readTree(line)
                val eventType = node.path("event").asText()
                val rawContent = node.path("content").toString()
                val parsedContent: Any? = objectMapper.readValue(rawContent, object : TypeReference<Any?>() {})
                emittedPayloads.add(rawContent)
                input.listener(
                    AgentEvent.ProtocolStreamEvent(
                        protocolId = input.protocol.id,
                        eventType = eventType,
                        payload = parsedContent,
                    )
                )
            },
            onError = { e -> future.completeExceptionally(e) },
            onComplete = { future.complete(Unit) },
        )

        model.chat(
            ChatRequest.builder().messages(messages).build(),
            object : StreamingChatResponseHandler {
                override fun onPartialResponse(partialResponse: String) {
                    buffer.onToken(partialResponse)
                }

                override fun onCompleteResponse(completeResponse: dev.langchain4j.model.chat.response.ChatResponse) {
                    buffer.complete()
                }

                override fun onError(error: Throwable) {
                    buffer.error(error)
                }
            }
        )

        future.join()
        val summary = emittedPayloads.joinToString("\n")
        return ProtocolExecutionResult(text = summary)
    }
}





