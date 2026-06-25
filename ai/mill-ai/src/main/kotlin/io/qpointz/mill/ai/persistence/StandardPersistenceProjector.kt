package io.qpointz.mill.ai.persistence

import io.qpointz.mill.ai.core.artifact.PointerCardinality
import io.qpointz.mill.ai.core.artifact.ProtocolFinalBatch
import io.qpointz.mill.ai.core.capability.*
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
                profileId = event.profileId ?: return,
                artifactIds = artifactIds,
                createdAt = event.createdAt,
            )
        )
    }

    private fun persistArtifact(event: RoutedAgentEvent) {
        val conversationId = event.conversationId ?: return
        val expanded = expandArtifactEvents(event)
        if (expanded.isEmpty()) return

        val artifactIds = mutableListOf<String>()
        val now = Instant.now()
        expanded.forEach { expandedEvent ->
            val artifactId = UUID.randomUUID().toString()
            val kind = expandedEvent.content["persistKind"] as? String
                ?: expandedEvent.content["protocolId"] as? String
                ?: expandedEvent.kind
            val artifact = ArtifactRecord(
                artifactId = artifactId,
                conversationId = conversationId,
                runId = event.runId,
                kind = kind,
                payload = expandedEvent.content,
                turnId = event.turnId,
                pointerKeys = event.route.rule.artifactPointerKeys,
                createdAt = event.createdAt,
            )
            artifactStore.save(artifact)
            artifactIds += artifactId
            artifactObservers.forEach { observer ->
                runCatching { observer.onArtifactCreated(artifact) }
                    .onFailure { error ->
                        log.warn("Artifact observer failed for artifactId={}", artifact.artifactId, error)
                    }
            }
        }

        event.turnId?.let { conversationStore.attachArtifacts(conversationId, it, artifactIds) }

        when (event.route.rule.artifactPointerCardinality) {
            PointerCardinality.SINGLE -> {
                val latestId = artifactIds.last()
                event.route.rule.artifactPointerKeys.forEach { key ->
                    activeArtifactPointerStore.upsert(
                        ActiveArtifactPointer(
                            conversationId = conversationId,
                            pointerKey = key,
                            artifactId = latestId,
                            updatedAt = now,
                        ),
                    )
                }
            }
            PointerCardinality.MULTIPLE -> {
                event.route.rule.artifactPointerKeys.forEach { key ->
                    activeArtifactPointerStore.appendAll(conversationId, key, artifactIds, now)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun expandArtifactEvents(event: RoutedAgentEvent): List<RoutedAgentEvent> {
        val payload = event.content["payload"]
        val items = ProtocolFinalBatch.expandItemPayloads(payload)
        if (items.isEmpty()) return listOf(event)
        if (items.size == 1 && !ProtocolFinalBatch.isBatchEnvelope(payload)) {
            return listOf(event)
        }
        return items.map { item ->
            event.copy(content = event.content.toMutableMap().apply { put("payload", item) })
        }
    }
}





