package io.qpointz.mill.ai.autoconfigure

import dev.langchain4j.model.chat.StreamingChatModel
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.chat.LangChain4jChatRuntime
import io.qpointz.mill.ai.autoconfigure.dependencies.SpringCapabilityDependencyAssembler
import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatProperties
import io.qpointz.mill.ai.autoconfigure.config.AiProfileSeedProperties
import io.qpointz.mill.ai.autoconfigure.config.PropertiesBackedModelResolver
import io.qpointz.mill.ai.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.chat.PropertiesUserIdResolver
import io.qpointz.mill.ai.chat.UserIdResolver
import io.qpointz.mill.ai.memory.BoundedWindowMemoryStrategy
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.memory.InMemoryChatMemoryStore
import io.qpointz.mill.ai.memory.LlmMemoryStrategy
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.ArtifactObserver
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.InMemoryActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.ai.persistence.InMemoryChatRegistry
import io.qpointz.mill.ai.persistence.InMemoryConversationStore
import io.qpointz.mill.ai.persistence.InMemoryRunEventStore
import io.qpointz.mill.ai.persistence.RunEventStore
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.ai.capabilities.valuemapping.MockValueMappingResolver
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingResolver
import io.qpointz.mill.ai.dependencies.CapabilityDependencyAssembler
import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.runtime.langchain4j.ArtifactEmissionCoordinator
import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.profile.ResourceProfileRegistry
import org.springframework.core.io.ResourceLoader
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import io.qpointz.mill.ai.autoconfigure.providers.AiProvidersAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Core AI v3 autoconfiguration.
 *
 * Wires in-memory store fallbacks, the LLM model, the agent runtime, and the user-id
 * resolver. All beans are [ConditionalOnMissingBean] so callers can override any of them.
 *
 * Service-layer orchestration ([io.qpointz.mill.ai.service.UnifiedChatService]) is wired by
 * [io.qpointz.mill.ai.autoconfigure.AiV3ChatServiceAutoConfiguration], which depends on this
 * module's beans and on `mill-ai-service` types.
 */
@ConditionalOnAiEnabled
@AutoConfiguration
@AutoConfigureAfter(AiProvidersAutoConfiguration::class)
@Import(AiV3JpaConfiguration::class)
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
     * Profile registry from `mill.ai.profiles.seed.resources` (YAML `kind: AgentProfile`).
     * Empty resource list yields an empty registry for isolated tests.
     */
    @Bean
    @ConditionalOnMissingBean(ProfileRegistry::class)
    fun resourceProfileRegistry(
        seedProperties: AiProfileSeedProperties,
        resourceLoader: ResourceLoader,
    ): ProfileRegistry {
        val locations = seedProperties.resources
        return if (locations.isEmpty()) {
            ResourceProfileRegistry.parse("")
        } else {
            ResourceProfileRegistry.load(
                ResourceProfileRegistry.SeedInput { loc ->
                    resourceLoader.getResource(loc).inputStream
                },
                locations,
            )
        }
    }

    /** Shared artefact descriptor registry loaded from capability YAML manifests. */
    @Bean
    @ConditionalOnMissingBean(ArtifactDescriptorRegistry::class)
    fun artifactDescriptorRegistry(): ArtifactDescriptorRegistry = ArtifactDescriptorRegistry.loadDefault()

    /** Coordinator for OnToolSuccess protocol-final synthesis. */
    @Bean
    @ConditionalOnMissingBean(ArtifactEmissionCoordinator::class)
    fun artifactEmissionCoordinator(
        artifactDescriptorRegistry: ArtifactDescriptorRegistry,
    ): ArtifactEmissionCoordinator = ArtifactEmissionCoordinator(artifactDescriptorRegistry)

    /**
     * Weak [ValueMappingResolver] used when no host metadata-backed resolver is registered.
     * `value-mapping` tools return empty or pass-through results until replaced.
     */
    @Bean
    @ConditionalOnMissingBean(ValueMappingResolver::class)
    fun stubValueMappingResolver(): ValueMappingResolver = MockValueMappingResolver()

    /**
     * Assembles [io.qpointz.mill.ai.runtime.AgentContext.capabilityDependencies] for chat turns from
     * optional data/SQL beans (see [AiV3DataAutoConfiguration] and [AiV3MetadataReadPortFallbackAutoConfiguration]).
     */
    @Bean
    @ConditionalOnMissingBean(CapabilityDependencyAssembler::class)
    fun capabilityDependencyAssembler(
        schemaCatalog: ObjectProvider<SchemaCatalogPort>,
        metadataReadPort: ObjectProvider<io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort>,
        conceptCatalog: ObjectProvider<io.qpointz.mill.ai.capabilities.concept.ConceptCatalogPort>,
        dialectSpec: ObjectProvider<SqlDialectSpec>,
        sqlValidator: ObjectProvider<SqlValidator>,
        sqlValidationService: ObjectProvider<SqlQueryToolHandlers.SqlValidationService>,
        sqlQueryExecutionPort: ObjectProvider<io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionPort>,
        sqlQueryExecutionProperties: ObjectProvider<io.qpointz.mill.ai.autoconfigure.sqlquery.SqlQueryExecutionProperties>,
        valueMappingResolver: ObjectProvider<ValueMappingResolver>,
    ): CapabilityDependencyAssembler = SpringCapabilityDependencyAssembler(
        schemaCatalog = schemaCatalog,
        metadataReadPort = metadataReadPort,
        conceptCatalog = conceptCatalog,
        dialectSpec = dialectSpec,
        sqlValidator = sqlValidator,
        sqlValidationService = sqlValidationService,
        sqlQueryExecutionPort = sqlQueryExecutionPort,
        sqlQueryExecutionProperties = sqlQueryExecutionProperties,
        valueMappingResolver = valueMappingResolver,
    )

    // ── LLM model ─────────────────────────────────────────────────────────────

    /**
     * Streaming chat model from `mill.ai.chat.model` + `mill.ai.models.chat` + `mill.ai.providers`.
     * Override to use a different provider or a LangChain4j model mock.
     */
    @Bean
    @ConditionalOnMissingBean(StreamingChatModel::class)
    fun streamingChatModel(modelResolver: PropertiesBackedModelResolver): StreamingChatModel =
        modelResolver.streamingChatModel()

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
        artifactDescriptorRegistry: ArtifactDescriptorRegistry,
        artifactObservers: ObjectProvider<ArtifactObserver>,
        metadataScopeService: ObjectProvider<io.qpointz.mill.metadata.service.MetadataScopeService>,
        chatProperties: AiV3ChatProperties,
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
        artifactDescriptorRegistry = artifactDescriptorRegistry,
        artifactObservers = artifactObservers.orderedStream().toList(),
        metadataScopeService = metadataScopeService.ifAvailable,
        scenarioCaptureEnabled = chatProperties.scenarioCapture.enabled,
        maxIterations = chatProperties.maxIterations,
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
