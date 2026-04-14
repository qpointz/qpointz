package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.autoconfigure.chat.MillAiV3ChatProperties
import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.UserIdResolver
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.service.ChatService
import io.qpointz.mill.ai.service.UnifiedChatService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Wires [UnifiedChatService] for the AI v3 HTTP API (`mill-ai-v3-service`).
 *
 * Declared after [MillAiV3AutoConfiguration] so store/runtime beans exist before the service
 * is constructed. Applications depend on **`mill-ai-v3-autoconfigure`** as the assembly
 * point; it pulls `mill-ai-v3-service` transitively.
 */
@AutoConfiguration(after = [MillAiV3AutoConfiguration::class])
class MillAiV3ChatServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChatService::class)
    fun unifiedChatService(
        registry: ChatRegistry,
        conversationStore: ConversationStore,
        chatMemoryStore: ChatMemoryStore,
        runtime: AiV3ChatRuntime,
        properties: MillAiV3ChatProperties,
        userIdResolver: UserIdResolver,
    ): UnifiedChatService = UnifiedChatService(
        registry = registry,
        conversationStore = conversationStore,
        chatMemoryStore = chatMemoryStore,
        runtime = runtime,
        properties = properties.toSettings(),
        userIdResolver = userIdResolver,
    )
}
