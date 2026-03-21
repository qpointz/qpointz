package io.qpointz.mill.ai.autoconfigure.chat

import io.qpointz.mill.ai.persistence.ChatMetadata
import reactor.core.publisher.Flux

/**
 * Primary extension point for AI agent execution.
 *
 * Implementations receive the resolved [ChatMetadata] (from which they derive the profile,
 * agent context, and any other runtime state) and the user's message text, and return a
 * reactive stream of [ChatRuntimeEvent]s.
 *
 * The default implementation wired by autoconfiguration is [LangChain4jChatRuntime].
 * Replace the bean to plug in a different LLM provider, a mock, or a test stub:
 *
 * ```kotlin
 * @Bean
 * fun myRuntime(): AiV3ChatRuntime = AiV3ChatRuntime { metadata, message ->
 *     Flux.just(ChatRuntimeEvent.Completed("echo: $message"))
 * }
 * ```
 *
 * @param metadata resolved chat metadata (profileId, contextType, contextId, …)
 * @param message the raw user message text for this turn
 * @return a cold [Flux] of [ChatRuntimeEvent]s; subscribed once per [sendMessage] call
 */
fun interface AiV3ChatRuntime {
    fun send(metadata: ChatMetadata, message: String): Flux<ChatRuntimeEvent>
}
