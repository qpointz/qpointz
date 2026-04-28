package io.qpointz.mill.ai.autoconfigure

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.chat.LangChain4jChatRuntime
import io.qpointz.mill.ai.autoconfigure.dependencies.SpringCapabilityDependencyAssembler
import io.qpointz.mill.ai.autoconfigure.chat.AiModelProperties
import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatProperties
import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.PropertiesUserIdResolver
import io.qpointz.mill.ai.chat.UserIdResolver
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
import io.qpointz.mill.ai.capabilities.metadata.EmptyMetadataReadPort
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.ai.capabilities.valuemapping.MockValueMappingResolver
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingResolver
import io.qpointz.mill.ai.dependencies.CapabilityDependencyAssembler
import io.qpointz.mill.ai.profile.DefaultProfileRegistry
import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.runtime.langchain4j.resolvedOpenAiBaseUrl
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.springframework.beans.factory.ObjectProvider
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
 * Service-layer orchestration ([io.qpointz.mill.ai.service.UnifiedChatService]) is wired by
 * [io.qpointz.mill.ai.autoconfigure.AiV3ChatServiceAutoConfiguration], which depends on this
 * module's beans and on `mill-ai-v3-service` types.
 */
@ConditionalOnAiEnabled
@AutoConfiguration
@Import(AiV3JpaConfiguration::class)
@EnableConfigurationProperties(AiModelProperties::class, AiV3ChatProperties::class)
class AiV3AutoConfiguration {

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

    /**
     * Weak [ValueMappingResolver] used when no host metadata-backed resolver is registered.
     * `value-mapping` tools return empty or pass-through results until replaced.
     */
    @Bean
    @ConditionalOnMissingBean(ValueMappingResolver::class)
    fun stubValueMappingResolver(): ValueMappingResolver = MockValueMappingResolver()

    /**
     * Assembles [io.qpointz.mill.ai.runtime.AgentContext.capabilityDependencies] for chat turns from
     * optional data/SQL beans (see [AiV3DataAutoConfiguration]).
     */
    @Bean
    @ConditionalOnMissingBean(MetadataReadPort::class)
    fun emptyMetadataReadPort(): MetadataReadPort = EmptyMetadataReadPort()

    @Bean
    @ConditionalOnMissingBean(CapabilityDependencyAssembler::class)
    fun capabilityDependencyAssembler(
        schemaCatalog: ObjectProvider<SchemaCatalogPort>,
        metadataReadPort: ObjectProvider<MetadataReadPort>,
        dialectSpec: ObjectProvider<SqlDialectSpec>,
        sqlValidator: ObjectProvider<SqlValidator>,
        sqlValidationService: ObjectProvider<SqlQueryToolHandlers.SqlValidationService>,
        valueMappingResolver: ObjectProvider<ValueMappingResolver>,
    ): CapabilityDependencyAssembler = SpringCapabilityDependencyAssembler(
        schemaCatalog = schemaCatalog,
        metadataReadPort = metadataReadPort,
        dialectSpec = dialectSpec,
        sqlValidator = sqlValidator,
        sqlValidationService = sqlValidationService,
        valueMappingResolver = valueMappingResolver,
    )

    // ── LLM model ─────────────────────────────────────────────────────────────

    /**
     * Streaming chat model configured from `mill.ai.model.*`.
     * Override to use a different provider or a LangChain4j model mock.
     */
    @Bean
    @ConditionalOnMissingBean(StreamingChatModel::class)
    fun streamingChatModel(props: AiModelProperties): StreamingChatModel =
        OpenAiStreamingChatModel.builder()
            .apiKey(props.apiKey)
            .modelName(props.modelName)
            .baseUrl(resolvedOpenAiBaseUrl(props.baseUrl))
            .build()

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
        capabilityDependencyAssembler: CapabilityDependencyAssembler,
        chatMemoryStore: ChatMemoryStore,
        memoryStrategy: LlmMemoryStrategy,
        conversationStore: ConversationStore,
        runEventStore: RunEventStore,
        artifactStore: ArtifactStore,
        activeArtifactPointerStore: ActiveArtifactPointerStore,
    ): AiV3ChatRuntime = LangChain4jChatRuntime(
        model = model,
        profileRegistry = profileRegistry,
        capabilityDependencyAssembler = capabilityDependencyAssembler,
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
    fun propertiesUserIdResolver(props: AiV3ChatProperties): UserIdResolver =
        PropertiesUserIdResolver(props.defaultUserId)
}
