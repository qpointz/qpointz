package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.autoconfigure.MillAiV3AutoConfiguration
import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.autoconfigure.chat.MillAiV3ChatProperties
import io.qpointz.mill.ai.autoconfigure.chat.UserIdResolver
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ConversationStore
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Wires the [UnifiedChatService] and [AiChatController] beans for the AI v3 service module.
 *
 * Declared after [MillAiV3AutoConfiguration] so all store/runtime beans are available
 * before [UnifiedChatService] is constructed.
 */
@AutoConfiguration(after = [MillAiV3AutoConfiguration::class])
class AiChatServiceConfiguration {

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
        properties = properties,
        userIdResolver = userIdResolver,
    )
}
