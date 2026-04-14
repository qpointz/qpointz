package io.qpointz.mill.ai.chat

/**
 * Service-layer streaming event from [AiV3ChatRuntime].
 *
 * Sits between the internal [io.qpointz.mill.ai.runtime.events.AgentEvent] (runtime-private)
 * and the presentation-layer [io.qpointz.mill.ai.sse.ChatSseEvent] (HTTP/SSE).
 * The controller maps these to [io.qpointz.mill.ai.sse.ChatSseEvent] for wire delivery.
 *
 * **Selective bridge from the runtime:** implementations choose which [AgentEvent]s become
 * [Chunk]/[Completed] (answer text), which become [Diagnostic] (UX status before [Completed]),
 * and which stay as structured [ToolCall]/[ToolResult] (optional detail for power users / debug).
 */
sealed interface ChatRuntimeEvent {
    /**
     * UX-oriented status line for the in-flight assistant turn, delivered before [Completed].
     *
     * Maps to SSE [io.qpointz.mill.ai.sse.ChatSseEvent.ItemDiagnostic] (`item.diagnostic`).
     * Prefer stable [code] values from [ChatDiagnosticCodes]; [detail] holds optional structured
     * context (e.g. profile id, plan mode) without exposing the full internal event log.
     */
    data class Diagnostic(
        val code: String,
        val message: String,
        val detail: Map<String, Any?>? = null,
    ) : ChatRuntimeEvent

    /** A streamed text fragment from the model. */
    data class Chunk(val text: String) : ChatRuntimeEvent

    /**
     * The model requested a tool invocation (developer-visible progress).
     *
     * @param name Registered tool name from the capability binding
     * @param arguments Parsed arguments for this call
     * @param iteration Zero-based index of the agent loop iteration that produced this request
     */
    data class ToolCall(
        val name: String,
        val arguments: Map<String, Any?>,
        val iteration: Int,
    ) : ChatRuntimeEvent

    /**
     * Structured result returned by a tool after [ToolCall].
     *
     * @param name Tool name matching the preceding [ToolCall]
     * @param result Structured payload produced by the tool handler (JSON-serialisable)
     */
    data class ToolResult(
        val name: String,
        val result: Any?,
    ) : ChatRuntimeEvent

    /** The model turn is complete. [text] is the full assembled response. */
    data class Completed(val text: String) : ChatRuntimeEvent
}
