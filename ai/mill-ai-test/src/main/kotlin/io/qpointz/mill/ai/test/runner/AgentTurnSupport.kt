package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.runtime.langchain4j.LangChain4jAgent
import io.qpointz.mill.ai.sse.AgentEventToSseMapper
import io.qpointz.mill.ai.sse.ChatSseEvent
import io.qpointz.mill.ai.test.scenario.v3.ArtifactSnapshot
import io.qpointz.mill.ai.test.scenario.v3.TranscriptSnapshot
import io.qpointz.mill.ai.test.scenario.v3.TranscriptTurnSnapshot
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome

/**
 * Shared turn execution and [TurnOutcome] assembly for scripted and live runners.
 */
internal object AgentTurnSupport {

    /**
     * Runs the agent for one user message and collects events, artefacts, SSE, and transcript.
     *
     * @param agent Configured LangChain4j agent.
     * @param persistence Persistence context wired into the agent.
     * @param session Conversation session.
     * @param ask User message for this turn.
     * @param logContext Optional turn context for activity logging.
     * @param onEvent Optional hook invoked for each emitted [AgentEvent] during the run.
     * @return Collected turn outcome.
     */
    fun collectTurnOutcome(
        agent: LangChain4jAgent,
        persistence: AgentPersistenceContext,
        session: ConversationSession,
        ask: String,
        context: io.qpointz.mill.ai.runtime.AgentContext = io.qpointz.mill.ai.runtime.AgentContext(contextType = "general"),
        logContext: ScenarioActivityLogger.TurnContext? = null,
        onEvent: (AgentEvent) -> Unit = {},
    ): TurnOutcome {
        logContext?.let { ScenarioActivityLogger.logTurnStarted(it, ask) }

        val events = mutableListOf<AgentEvent>()
        val sseMapper = AgentEventToSseMapper(session.conversationId)
        val sseEvents = mutableListOf<ChatSseEvent>()

        val response = agent.run(ask, session, context) { event ->
            events.add(event)
            logContext?.let { ScenarioActivityLogger.logAgentEvent(it, event) }
            onEvent(event)
            sseEvents.addAll(sseMapper.map(event))
        }

        val artifacts = persistence.artifactStore
            .findByConversation(session.conversationId)
            .map { it.toSnapshot() }

        val transcriptRecord = persistence.conversationStore.load(session.conversationId)
        val transcript = transcriptRecord?.let {
            TranscriptSnapshot(
                turnCount = it.turns.size,
                turns = it.turns.map { turn ->
                    TranscriptTurnSnapshot(
                        role = turn.role,
                        text = turn.text,
                        artifactIds = turn.artifactIds,
                    )
                },
            )
        }

        val outcome = TurnOutcome(
            response = response,
            events = events.toList(),
            artifacts = artifacts,
            sseEvents = sseEvents.toList(),
            structuredParts = sseEvents.mapNotNull { event ->
                if (event is ChatSseEvent.ItemPartUpdated && event.presentation == "structured") {
                    io.qpointz.mill.ai.test.scenario.v3.StructuredPartSnapshot(
                        presentation = event.presentation,
                        partType = event.partType,
                        content = event.content,
                    )
                } else {
                    null
                }
            },
            transcript = transcript,
        )
        logContext?.let { ScenarioActivityLogger.logTurnCompleted(it, outcome) }
        return outcome
    }

    private fun ArtifactRecord.toSnapshot(): ArtifactSnapshot =
        ArtifactSnapshot(
            persistKind = kind,
            artifactId = artifactId,
            payload = payload,
        )
}
