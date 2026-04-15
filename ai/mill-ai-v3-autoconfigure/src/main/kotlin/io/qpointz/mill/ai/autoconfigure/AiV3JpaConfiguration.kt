package io.qpointz.mill.ai.autoconfigure

import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.domain.EntityScanPackages
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata

/**
 * Registers JPA-backed ai/v3 store adapters when mill-ai-v3-persistence is on the classpath.
 *
 * All beans are conditional on missing user-defined beans, ensuring user overrides win.
 */
@Configuration
@ConditionalOnClass(name = ["io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatMemoryStore"])
@Import(
    AiV3JpaConfiguration.AiV3PackageRegistrar::class,
    AiV3JpaRepositoriesConfiguration::class,
)
class AiV3JpaConfiguration {

    internal class AiV3PackageRegistrar : ImportBeanDefinitionRegistrar {
        override fun registerBeanDefinitions(metadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
            EntityScanPackages.register(registry, "io.qpointz.mill.persistence.ai.jpa.entities")
            AutoConfigurationPackages.register(registry, "io.qpointz.mill.persistence.ai.jpa")
        }
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(io.qpointz.mill.ai.memory.ChatMemoryStore::class)
    fun jpaChatMemoryStore(
        memoryRepo: io.qpointz.mill.persistence.ai.jpa.repositories.ChatMemoryRepository,
        messageRepo: io.qpointz.mill.persistence.ai.jpa.repositories.ChatMemoryMessageRepository,
    ): io.qpointz.mill.ai.memory.ChatMemoryStore =
        io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatMemoryStore(memoryRepo, messageRepo)

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(io.qpointz.mill.ai.persistence.RunEventStore::class)
    fun jpaRunEventStore(
        repo: io.qpointz.mill.persistence.ai.jpa.repositories.RunEventRepository,
    ): io.qpointz.mill.ai.persistence.RunEventStore =
        io.qpointz.mill.persistence.ai.jpa.adapters.JpaRunEventStore(repo)

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(io.qpointz.mill.ai.persistence.ConversationStore::class)
    fun jpaConversationStore(
        conversationRepo: io.qpointz.mill.persistence.ai.jpa.repositories.ConversationRepository,
        turnRepo: io.qpointz.mill.persistence.ai.jpa.repositories.ConversationTurnRepository,
        relationRepo: io.qpointz.mill.persistence.ai.jpa.repositories.AiRelationRepository,
        artifactRepo: io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository,
    ): io.qpointz.mill.ai.persistence.ConversationStore =
        io.qpointz.mill.persistence.ai.jpa.adapters.JpaConversationStore(conversationRepo, turnRepo, relationRepo, artifactRepo)

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(io.qpointz.mill.ai.persistence.ArtifactStore::class)
    fun jpaArtifactStore(
        repo: io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository,
    ): io.qpointz.mill.ai.persistence.ArtifactStore =
        io.qpointz.mill.persistence.ai.jpa.adapters.JpaArtifactStore(repo)

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore::class)
    fun jpaActiveArtifactPointerStore(
        repo: io.qpointz.mill.persistence.ai.jpa.repositories.ActiveArtifactPointerRepository,
    ): io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore =
        io.qpointz.mill.persistence.ai.jpa.adapters.JpaActiveArtifactPointerStore(repo)

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnBean(
        io.qpointz.mill.persistence.ai.jpa.repositories.AiEmbeddingModelRepository::class,
    )
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
        io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository::class,
    )
    fun jpaValueMappingEmbeddingRepository(
        modelRepo: io.qpointz.mill.persistence.ai.jpa.repositories.AiEmbeddingModelRepository,
        valueRepo: io.qpointz.mill.persistence.ai.jpa.repositories.AiValueMappingRepository,
    ): io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository =
        io.qpointz.mill.persistence.ai.jpa.adapters.JpaValueMappingEmbeddingAdapter(modelRepo, valueRepo)
}
