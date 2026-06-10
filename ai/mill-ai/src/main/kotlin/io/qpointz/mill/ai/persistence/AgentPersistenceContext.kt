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

/**
 * Groups the four durable persistence ports and wires [StandardPersistenceProjector]
 * as a listener on the supplied [publisher].
 *
 * Construct once per agent instance and pass to the runtime.
 */
class AgentPersistenceContext(
    val runEventStore: RunEventStore = InMemoryRunEventStore(),
    val conversationStore: ConversationStore = InMemoryConversationStore(),
    val artifactStore: ArtifactStore = InMemoryArtifactStore(),
    val activeArtifactPointerStore: ActiveArtifactPointerStore = InMemoryActiveArtifactPointerStore(),
    val publisher: AgentEventPublisher = InMemoryAgentEventPublisher(),
    val telemetryAccumulator: RunTelemetryAccumulator = RunTelemetryAccumulator(),
    val artifactObservers: List<ArtifactObserver> = listOf(NoOpArtifactObserver()),
) {
    init {
        publisher.register(
            StandardPersistenceProjector(
                runEventStore = runEventStore,
                conversationStore = conversationStore,
                artifactStore = artifactStore,
                activeArtifactPointerStore = activeArtifactPointerStore,
                artifactObservers = artifactObservers,
            )
        )
        publisher.register(telemetryAccumulator)
    }
}





