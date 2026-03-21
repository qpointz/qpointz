package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.autoconfigure.chat.ChatRuntimeEvent
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
 * - [ChatRuntimeEvent.Completed] → emit [ChatSseEvent.ItemCompleted];
 *   `content` is `null` when chunks were emitted (streaming consumers use accumulated text),
 *   `content` is the full text when no chunks preceded (non-streaming path).
 */
class ChatRuntimeEventToSseMapper(private val chatId: String) {

    private val sequence = AtomicInteger(0)
    private var itemId: String = UUID.randomUUID().toString()
    private var chunksEmitted = false

    fun map(event: ChatRuntimeEvent): List<ChatSseEvent> = when (event) {
        is ChatRuntimeEvent.Chunk -> {
            val result = mutableListOf<ChatSseEvent>()
            if (!chunksEmitted) {
                chunksEmitted = true
                result += itemCreated()
            }
            result += itemPartUpdated(event.text)
            result
        }
        is ChatRuntimeEvent.Completed -> {
            val result = mutableListOf<ChatSseEvent>()
            val hadChunks = chunksEmitted
            if (!chunksEmitted) {
                chunksEmitted = true
                result += itemCreated()
            }
            // Null content when chunks were streamed: clients use accumulated deltas.
            // Full content only for the non-streaming (single-shot) path.
            result += itemCompleted(if (hadChunks) null else event.text)
            itemId = UUID.randomUUID().toString()
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

    private fun itemCompleted(content: String?) = ChatSseEvent.ItemCompleted(
        eventId = UUID.randomUUID().toString(),
        chatId = chatId,
        itemId = itemId,
        sequence = sequence.getAndIncrement(),
        timestamp = Instant.now(),
        content = content,
    )
}
