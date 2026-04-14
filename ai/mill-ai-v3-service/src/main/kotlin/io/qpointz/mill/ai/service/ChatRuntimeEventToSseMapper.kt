package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.sse.ChatSseEvent
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Stateful mapper from service-layer [ChatRuntimeEvent] to presentation-layer [ChatSseEvent].
 *
 * One instance covers one [UnifiedChatService.sendMessage] call. Create a new instance per call.
 *
 * Mapping rules:
 * - First [ChatRuntimeEvent.Chunk] → emit [ChatSseEvent.ItemCreated] then [ChatSseEvent.ItemPartUpdated]
 * - Subsequent [ChatRuntimeEvent.Chunk] → emit [ChatSseEvent.ItemPartUpdated]
 * - [ChatRuntimeEvent.Diagnostic] → emit [ChatSseEvent.ItemDiagnostic] (`item.diagnostic`), opening
 *   the assistant item with [ChatSseEvent.ItemCreated] if needed (same correlation as other pre-reply events)
 * - [ChatRuntimeEvent.ToolCall] / [ChatRuntimeEvent.ToolResult] → emit [ChatSseEvent.ItemToolCall] /
 *   [ChatSseEvent.ItemToolResult], opening the assistant item with [ChatSseEvent.ItemCreated] if needed
 * - [ChatRuntimeEvent.Completed] → emit [ChatSseEvent.ItemCompleted];
 *   `content` is `null` when chunks were emitted (streaming consumers use accumulated text),
 *   `content` is the full text when no chunks preceded (non-streaming path).
 */
class ChatRuntimeEventToSseMapper(private val chatId: String) {

    private val sequence = AtomicInteger(0)
    private var itemId: String = UUID.randomUUID().toString()
    /** Whether [ChatSseEvent.ItemCreated] was emitted for the current [itemId]. */
    private var itemStarted = false
    /** Whether at least one text [ChatRuntimeEvent.Chunk] was emitted for the current item. */
    private var chunksEmitted = false

    fun map(event: ChatRuntimeEvent): List<ChatSseEvent> = when (event) {
        is ChatRuntimeEvent.Chunk -> {
            val result = mutableListOf<ChatSseEvent>()
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            chunksEmitted = true
            result += itemPartUpdated(event.text)
            result
        }
        is ChatRuntimeEvent.Diagnostic -> {
            val result = mutableListOf<ChatSseEvent>()
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            result += itemDiagnostic(event.code, event.message, event.detail)
            result
        }
        is ChatRuntimeEvent.ToolCall -> {
            val result = mutableListOf<ChatSseEvent>()
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            result += itemToolCall(event.name, event.arguments, event.iteration)
            result
        }
        is ChatRuntimeEvent.ToolResult -> {
            val result = mutableListOf<ChatSseEvent>()
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            result += itemToolResult(event.name, event.result)
            result
        }
        is ChatRuntimeEvent.Completed -> {
            val result = mutableListOf<ChatSseEvent>()
            val hadChunks = chunksEmitted
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            // Null content when chunks were streamed: clients use accumulated deltas.
            // Full content only for the non-streaming (single-shot) path.
            result += itemCompleted(if (hadChunks) null else event.text)
            itemId = UUID.randomUUID().toString()
            itemStarted = false
            chunksEmitted = false
            result
        }
    }

    fun fail(code: String, reason: String): ChatSseEvent.ItemFailed = ChatSseEvent.ItemFailed(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
        code = code,
        reason = reason,
    )

    private fun itemCreated() = ChatSseEvent.ItemCreated(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
    )

    private fun itemPartUpdated(content: String) = ChatSseEvent.ItemPartUpdated(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
        content = content,
    )

    private fun itemDiagnostic(code: String, message: String, detail: Map<String, Any?>?) =
        ChatSseEvent.ItemDiagnostic(
            eventId = UUID.randomUUID().toString(),
            chatId = chatId,
            itemId = itemId,
            sequence = sequence.getAndIncrement(),
            timestamp = Instant.now(),
            code = code,
            message = message,
            detail = detail,
        )

    private fun itemToolCall(name: String, arguments: Map<String, Any?>, iteration: Int) =
        ChatSseEvent.ItemToolCall(
            eventId = UUID.randomUUID().toString(),
            chatId = chatId,
            itemId = itemId,
            sequence = sequence.getAndIncrement(),
            timestamp = Instant.now(),
            toolName = name,
            arguments = arguments,
            iteration = iteration,
        )

    private fun itemToolResult(name: String, result: Any?) = ChatSseEvent.ItemToolResult(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
        toolName = name,
        result = result,
    )

    private fun itemCompleted(content: String?) = ChatSseEvent.ItemCompleted(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
        content = content,
    )
}
