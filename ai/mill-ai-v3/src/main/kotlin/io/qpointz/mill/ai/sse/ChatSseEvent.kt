package io.qpointz.mill.ai.sse

import java.time.Instant

/**
 * Presentation-oriented SSE event model for AI chat streaming.
 *
 * These events are public transport contract — not internal agent lifecycle events.
 * Consumers (mill-ui, CLI) reconstruct logical assistant items from the ordered sequence
 * of events sharing the same [itemId].
 *
 * V1 frozen values: [presentation] = "conversation", [partType] = "text".
 * Future iterations may introduce presentation = "structured" and partType = "sql" / "chart" / "data".
 */
sealed interface ChatSseEvent {
    val eventId: String
    val chatId: String
    val itemId: String
    val sequence: Int
    val type: String
    val timestamp: Instant

    /**
     * A new logical assistant output item has started.
     * Consumers should allocate a rendering slot keyed by [itemId].
     */
    data class ItemCreated(
        override val eventId: String,
        override val chatId: String,
        override val itemId: String,
        override val sequence: Int,
        override val timestamp: Instant,
        val presentation: String = "conversation",
    ) : ChatSseEvent {
        override val type: String = "item.created"
    }

    /**
     * A partial content update for an in-progress item.
     * [mode] = "append" means the consumer should append [content] to the accumulated text.
     * Future modes: "replace", "patch".
     */
    data class ItemPartUpdated(
        override val eventId: String,
        override val chatId: String,
        override val itemId: String,
        override val sequence: Int,
        override val timestamp: Instant,
        val presentation: String = "conversation",
        val partType: String = "text",
        val mode: String = "append",
        val content: String,
    ) : ChatSseEvent {
        override val type: String = "item.part.updated"
    }

    /**
     * The item is complete. No further parts will arrive for this [itemId].
     *
     * Reconstruction contract:
     * - **Streaming consumers** (received [ItemPartUpdated] deltas): ignore [content]; use their
     *   accumulated text. [content] will be `null` in this case.
     * - **Non-streaming consumers** (no deltas preceded this event): use [content] directly.
     *   [content] will be non-null in this case.
     *
     * This avoids a double-render bug where a client that buffered all deltas also replaces
     * the accumulated text with the full [content] string.
     */
    data class ItemCompleted(
        override val eventId: String,
        override val chatId: String,
        override val itemId: String,
        override val sequence: Int,
        override val timestamp: Instant,
        val presentation: String = "conversation",
        val partType: String = "text",
        val content: String?,
    ) : ChatSseEvent {
        override val type: String = "item.completed"
    }

    /**
     * The item failed before completion. Consumers should render an error state for [itemId].
     *
     * [code] is a stable dot-separated machine-readable token consumers can switch on
     * without parsing text (e.g. "runtime.unavailable", "agent.error", "auth.forbidden").
     * [reason] is a human-readable description for logging / display.
     */
    data class ItemFailed(
        override val eventId: String,
        override val chatId: String,
        override val itemId: String,
        override val sequence: Int,
        override val timestamp: Instant,
        val code: String,
        val reason: String,
    ) : ChatSseEvent {
        override val type: String = "item.failed"
    }
}
