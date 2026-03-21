package io.qpointz.mill.ai.sse

import io.qpointz.mill.ai.runtime.events.AgentEvent
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Stateful mapper from internal [AgentEvent] stream to public [ChatSseEvent] stream.
 *
 * One mapper instance covers one chat turn. Create a new instance per [sendMessage] call.
 *
 * Mapping rules (V1 text path):
 * - First [AgentEvent.MessageDelta] in a turn → emit [ChatSseEvent.ItemCreated] then [ChatSseEvent.ItemPartUpdated]
 * - Subsequent [AgentEvent.MessageDelta] → emit [ChatSseEvent.ItemPartUpdated]
 * - [AgentEvent.AnswerCompleted] → emit [ChatSseEvent.ItemCompleted]; reset for next item
 * - All other [AgentEvent] subtypes → ignored at this layer (runtime-internal)
 */
class AgentEventToSseMapper(private val chatId: String) {

    private val sequence = AtomicInteger(0)
    private var itemId: String = UUID.randomUUID().toString()
    private var itemStarted = false

    fun map(event: AgentEvent): List<ChatSseEvent> = when (event) {
        is AgentEvent.MessageDelta -> {
            val result = mutableListOf<ChatSseEvent>()
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            result += itemPartUpdated(event.text)
            result
        }

        is AgentEvent.AnswerCompleted -> {
            val result = mutableListOf<ChatSseEvent>()
            val hadDeltas = itemStarted
            if (!itemStarted) {
                itemStarted = true
                result += itemCreated()
            }
            // Pass full content only when no deltas were emitted (non-streaming consumers).
            // Streaming consumers accumulated deltas and must ignore content to avoid double-render.
            result += itemCompleted(if (hadDeltas) null else event.text)
            // Reset for next item in the same turn (e.g. multi-step agents)
            itemId = UUID.randomUUID().toString()
            itemStarted = false
            result
        }

        else -> emptyList()
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
