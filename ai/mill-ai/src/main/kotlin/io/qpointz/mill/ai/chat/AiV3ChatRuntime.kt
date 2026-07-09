package io.qpointz.mill.ai.chat

import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.runtime.TurnContextValues
import reactor.core.publisher.Flux

/**
 * Primary extension point for AI agent execution.
 *
 * Implementations receive the resolved [ChatMetadata] (from which they derive the profile,
 * agent context, and any other runtime state) and the user's message text, and return a
 * reactive stream of [ChatRuntimeEvent]s.
 *
 * Spring Boot hosts register a default implementation via `mill-ai-autoconfigure`
 * (typically LangChain4j-backed). Replace the bean to plug in a different LLM provider,
 * a mock, or a test stub.
 *
 * @param metadata resolved chat metadata (profileId, contextType, contextId, …)
 * @param message the raw user message text for this turn
 * @param turnContext optional ephemeral host context for this turn (`context.values` on the wire)
 * @return a cold [Flux] of [ChatRuntimeEvent]s; subscribed once per send call
 */
fun interface AiV3ChatRuntime {
    fun send(
        metadata: ChatMetadata,
        message: String,
        turnContext: TurnContextValues?,
    ): Flux<ChatRuntimeEvent>
}

/** Sends a message with no turn context. */
fun AiV3ChatRuntime.send(metadata: ChatMetadata, message: String): Flux<ChatRuntimeEvent> =
    send(metadata, message, null)
