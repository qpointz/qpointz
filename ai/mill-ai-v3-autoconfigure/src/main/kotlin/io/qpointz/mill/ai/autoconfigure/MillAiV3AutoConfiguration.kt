package io.qpointz.mill.ai.autoconfigure

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.autoconfigure.chat.LangChain4jChatRuntime
import io.qpointz.mill.ai.autoconfigure.chat.MillAiModelProperties
import io.qpointz.mill.ai.autoconfigure.chat.MillAiV3ChatProperties
import io.qpointz.mill.ai.autoconfigure.chat.PropertiesUserIdResolver
import io.qpointz.mill.ai.autoconfigure.chat.UserIdResolver
import io.qpointz.mill.ai.memory.BoundedWindowMemoryStrategy
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.memory.InMemoryChatMemoryStore
import io.qpointz.mill.ai.memory.LlmMemoryStrategy
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.InMemoryActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.ai.persistence.InMemoryChatRegistry
import io.qpointz.mill.ai.persistence.InMemoryConversationStore
import io.qpointz.mill.ai.persistence.InMemoryRunEventStore
import io.qpointz.mill.ai.persistence.RunEventStore
import io.qpointz.mill.ai.profile.DefaultProfileRegistry
import io.qpointz.mill.ai.profile.ProfileRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Core AI v3 autoconfiguration.
 *
 * Wires in-memory store fallbacks, the LLM model, the agent runtime, and the user-id
 * resolver. All beans are [ConditionalOnMissingBean] so callers can override any of them.
 *
 * Service-layer orchestration ([io.qpointz.mill.ai.service.UnifiedChatService]) is wired
 * by the dependent `mill-ai-v3-service` module's own autoconfiguration, keeping this
 * module strictly to bean wiring and configuration.
 */
@AutoConfiguration
@Import(MillAiV3JpaConfiguration::class)
@EnableConfigurationProperties(MillAiModelProperties::class, MillAiV3ChatProperties::class)
class MillAiV3AutoConfiguration {

    // ── Store defaults (in-memory fallbacks) ──────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(ChatMemoryStore::class)
    fun inMemoryChatMemoryStore(): ChatMemoryStore = InMemoryChatMemoryStore()

    @Bean
    @ConditionalOnMissingBean(LlmMemoryStrategy::class)
    fun boundedWindowMemoryStrategy(): LlmMemoryStrategy = BoundedWindowMemoryStrategy()

    @Bean
    @ConditionalOnMissingBean(RunEventStore::class)
    fun inMemoryRunEventStore(): RunEventStore = InMemoryRunEventStore()

    @Bean
    @ConditionalOnMissingBean(ConversationStore::class)
    fun inMemoryConversationStore(): ConversationStore = InMemoryConversationStore()

    @Bean
    @ConditionalOnMissingBean(ArtifactStore::class)
    fun inMemoryArtifactStore(): ArtifactStore = InMemoryArtifactStore()

    @Bean
    @ConditionalOnMissingBean(ActiveArtifactPointerStore::class)
    fun inMemoryActiveArtifactPointerStore(): ActiveArtifactPointerStore = InMemoryActiveArtifactPointerStore()

    @Bean
    @ConditionalOnMissingBean(ChatRegistry::class)
    fun inMemoryChatRegistry(): ChatRegistry = InMemoryChatRegistry()

    // ── Profile registry ──────────────────────────────────────────────────────

    /**
     * Default profile registry backed by [DefaultProfileRegistry].
     * Override to add custom profiles or load them from a database.
     */
    @Bean
    @ConditionalOnMissingBean(ProfileRegistry::class)
    fun defaultProfileRegistry(): ProfileRegistry = DefaultProfileRegistry

    // ── LLM model ─────────────────────────────────────────────────────────────

    /**
     * Streaming chat model configured from `mill.ai.model.*`.
     * Override to use a different provider or a LangChain4j model mock.
     */
    @Bean
    @ConditionalOnMissingBean(StreamingChatModel::class)
    fun streamingChatModel(props: MillAiModelProperties): StreamingChatModel {
        val builder = OpenAiStreamingChatModel.builder()
            .apiKey(props.apiKey)
            .modelName(props.modelName)
        props.baseUrl?.let { builder.baseUrl(it) }
        return builder.build()
    }

    // ── Chat runtime ──────────────────────────────────────────────────────────

    /**
     * LangChain4j-backed [AiV3ChatRuntime].
     * Override to plug in a different LLM provider or a test stub.
     */
    @Bean
    @ConditionalOnMissingBean(AiV3ChatRuntime::class)
    fun langChain4jChatRuntime(
        model: StreamingChatModel,
        profileRegistry: ProfileRegistry,
        chatMemoryStore: ChatMemoryStore,
        memoryStrategy: LlmMemoryStrategy,
        conversationStore: ConversationStore,
        runEventStore: RunEventStore,
        artifactStore: ArtifactStore,
        activeArtifactPointerStore: ActiveArtifactPointerStore,
    ): AiV3ChatRuntime = LangChain4jChatRuntime(
        model = model,
        profileRegistry = profileRegistry,
        chatMemoryStore = chatMemoryStore,
        memoryStrategy = memoryStrategy,
        runEventStore = runEventStore,
        conversationStore = conversationStore,
        artifactStore = artifactStore,
        activeArtifactPointerStore = activeArtifactPointerStore,
    )

    // ── User identity ─────────────────────────────────────────────────────────

    /**
     * Fallback [UserIdResolver] returning the static value from `mill.ai.chat.default-user-id`.
     * Replace with a Spring Security-aware implementation for multi-user deployments.
     */
    @Bean
    @ConditionalOnMissingBean(UserIdResolver::class)
    fun propertiesUserIdResolver(props: MillAiV3ChatProperties): UserIdResolver =
        PropertiesUserIdResolver(props.defaultUserId)
}
