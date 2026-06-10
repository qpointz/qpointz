package io.qpointz.mill.ai.persistence

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import java.time.Instant
import java.util.UUID
import org.slf4j.LoggerFactory

/**
 * Listens on the routed event bus and projects events into the four durable stores.
 *
 * - [RoutedEventDestination.ARTIFACT] → [ArtifactStore] + [ActiveArtifactPointerStore]
 * - [RoutedEventDestination.CHAT_TRANSCRIPT] → [ConversationStore]
 * - [EventRoutingRule.persistEvent] → [RunEventStore]
 *
 * Registered as a listener on [AgentEventPublisher] by [AgentPersistenceContext].
 */
class StandardPersistenceProjector(
    private val runEventStore: RunEventStore,
    private val conversationStore: ConversationStore,
    private val artifactStore: ArtifactStore,
    private val activeArtifactPointerStore: ActiveArtifactPointerStore,
    private val artifactObservers: List<ArtifactObserver> = emptyList(),
) : AgentEventListener {

    companion object {
        private val log = LoggerFactory.getLogger(StandardPersistenceProjector::class.java)
    }

    override fun onEvent(event: RoutedAgentEvent) {
        if (event.route.rule.persistEvent && event.runId != null) {
            persistRunEvent(event)
        }
        if (event.destinations.contains(RoutedEventDestination.CHAT_TRANSCRIPT) && event.route.rule.persistAsTranscript) {
            persistTranscriptTurn(event)
        }
        if (event.destinations.contains(RoutedEventDestination.ARTIFACT) && event.route.rule.persistAsArtifact) {
            persistArtifact(event)
        }
    }

    private fun persistRunEvent(event: RoutedAgentEvent) {
        runEventStore.save(
            RunEventRecord(
                eventId = event.eventId,
                runId = event.runId!!,
                conversationId = event.conversationId,
                profileId = event.profileId,
                kind = event.kind,
                runtimeType = event.runtimeType,
                content = event.content,
                createdAt = event.createdAt,
            )
        )
    }

    private fun persistTranscriptTurn(event: RoutedAgentEvent) {
        val conversationId = event.conversationId ?: return
        val text = event.content["text"] as? String
        val turnId = event.turnId ?: UUID.randomUUID().toString()
        // Link any artifacts that were already persisted for this turnId in the same run.
        val artifactIds = artifactStore.findByConversation(conversationId)
            .filter { it.turnId == turnId }
            .map { it.artifactId }
        conversationStore.appendTurn(
            conversationId,
            ConversationTurn(
                turnId = turnId,
                role = "assistant",
                text = text?.takeIf { it.isNotEmpty() },
                artifactIds = artifactIds,
                createdAt = event.createdAt,
            )
        )
    }

    private fun persistArtifact(event: RoutedAgentEvent) {
        val conversationId = event.conversationId ?: return
        val artifactId = UUID.randomUUID().toString()
        val kind = event.content["protocolId"] as? String ?: event.kind
        val artifact = ArtifactRecord(
            artifactId = artifactId,
            conversationId = conversationId,
            runId = event.runId,
            kind = kind,
            payload = event.content,
            turnId = event.turnId,
            pointerKeys = event.route.rule.artifactPointerKeys,
            createdAt = event.createdAt,
        )
        artifactStore.save(artifact)
        event.turnId?.let { conversationStore.attachArtifacts(conversationId, it, listOf(artifactId)) }
        val now = Instant.now()
        event.route.rule.artifactPointerKeys.forEach { key ->
            activeArtifactPointerStore.upsert(
                ActiveArtifactPointer(
                    conversationId = conversationId,
                    pointerKey = key,
                    artifactId = artifactId,
                    updatedAt = now,
                )
            )
        }
        artifactObservers.forEach { observer ->
            runCatching { observer.onArtifactCreated(artifact) }
                .onFailure { error ->
                    log.warn("Artifact observer failed for artifactId={}", artifact.artifactId, error)
                }
        }
    }
}





