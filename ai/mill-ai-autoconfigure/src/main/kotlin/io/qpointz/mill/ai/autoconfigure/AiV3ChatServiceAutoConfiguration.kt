package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatProperties
import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.UserIdResolver
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.service.ChatService
import io.qpointz.mill.ai.service.UnifiedChatService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Wires [UnifiedChatService] for the AI v3 HTTP API (`mill-ai-service`).
 *
 * Declared after [AiV3AutoConfiguration] so store/runtime beans exist before the service
 * is constructed. Applications depend on **`mill-ai-autoconfigure`** as the assembly
 * point; it pulls `mill-ai-service` transitively.
 */
@ConditionalOnAiEnabled
@AutoConfiguration(after = [AiV3AutoConfiguration::class])
class AiV3ChatServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChatService::class)
    fun unifiedChatService(
        registry: ChatRegistry,
        conversationStore: ConversationStore,
        chatMemoryStore: ChatMemoryStore,
        artifactStore: ArtifactStore,
        profileRegistry: ProfileRegistry,
        runtime: AiV3ChatRuntime,
        properties: AiV3ChatProperties,
        userIdResolver: UserIdResolver,
    ): UnifiedChatService = UnifiedChatService(
        registry = registry,
        conversationStore = conversationStore,
        chatMemoryStore = chatMemoryStore,
        artifactStore = artifactStore,
        profileRegistry = profileRegistry,
        runtime = runtime,
        properties = properties.toSettings(),
        userIdResolver = userIdResolver,
    )
}
