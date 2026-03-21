package io.qpointz.mill.ai.autoconfigure.chat

/**
 * Service-layer streaming event from [AiV3ChatRuntime].
 *
 * Sits between the internal [io.qpointz.mill.ai.runtime.events.AgentEvent] (runtime-private)
 * and the presentation-layer [io.qpointz.mill.ai.sse.ChatSseEvent] (HTTP/SSE).
 * The controller maps these to [io.qpointz.mill.ai.sse.ChatSseEvent] for wire delivery.
 */
sealed interface ChatRuntimeEvent {
    /** A streamed text fragment from the model. */
    data class Chunk(val text: String) : ChatRuntimeEvent

    /** The model turn is complete. [text] is the full assembled response. */
    data class Completed(val text: String) : ChatRuntimeEvent
}
