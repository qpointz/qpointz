package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.memory.BoundedWindowMemoryStrategy
import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.memory.InMemoryChatMemoryStore
import io.qpointz.mill.ai.memory.LlmMemoryStrategy
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.InMemoryActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.ai.persistence.InMemoryConversationStore
import io.qpointz.mill.ai.persistence.InMemoryRunEventStore
import io.qpointz.mill.ai.persistence.RunEventStore
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(MillAiV3JpaConfiguration::class)
class MillAiV3AutoConfiguration {

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
}
