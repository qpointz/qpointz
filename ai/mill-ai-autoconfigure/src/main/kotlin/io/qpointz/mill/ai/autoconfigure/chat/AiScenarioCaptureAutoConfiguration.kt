package io.qpointz.mill.ai.autoconfigure.chat

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.RunEventStore
import io.qpointz.mill.ai.scenario.ConversationScenarioExporter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

/**
 * Wires scenario capture export when `mill.ai.chat.scenario-capture.enabled=true`.
 */
@ConditionalOnAiEnabled
@ConditionalOnProperty(
    prefix = "mill.ai.chat.scenario-capture",
    name = ["enabled"],
    havingValue = "true",
)
@AutoConfiguration
class AiScenarioCaptureAutoConfiguration {

    /**
     * Builds draft scenario packs from persisted chat data.
     */
    @Bean
    fun conversationScenarioExporter(
        chatRegistry: ChatRegistry,
        conversationStore: ConversationStore,
        runEventStore: RunEventStore,
        artifactStore: ArtifactStore,
    ): ConversationScenarioExporter = ConversationScenarioExporter(
        chatRegistry = chatRegistry,
        conversationStore = conversationStore,
        runEventStore = runEventStore,
        artifactStore = artifactStore,
    )
}
