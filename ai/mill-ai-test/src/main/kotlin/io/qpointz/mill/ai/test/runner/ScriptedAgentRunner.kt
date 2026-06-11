package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.profile.DefaultProfileRegistry
import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.runtime.langchain4j.LangChain4jAgent
import io.qpointz.mill.ai.sse.AgentEventToSseMapper
import io.qpointz.mill.ai.sse.ChatSseEvent
import io.qpointz.mill.ai.test.scenario.v3.AskRunItem
import io.qpointz.mill.ai.test.scenario.v3.ArtifactSnapshot
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.TranscriptSnapshot
import io.qpointz.mill.ai.test.scenario.v3.TranscriptTurnSnapshot
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome

/**
 * Runs a single scenario turn using a scripted model and real [LangChain4jAgent] handlers.
 */
class ScriptedAgentRunner(
    private val profileRegistry: ProfileRegistry = DefaultProfileRegistry,
) {

    /**
     * Executes one `ask` turn from a pack.
     *
     * @param pack Parent scenario pack (profile + mode).
     * @param item Ask turn definition including script.
     * @param turnIndex Zero-based turn index for diagnostics.
     * @param session Conversation session (created if not supplied).
     * @return Collected turn outcome.
     */
    fun runTurn(
        pack: ScenarioPack,
        item: AskRunItem,
        turnIndex: Int,
        session: ConversationSession = ConversationSession(profileId = pack.profileId),
    ): TurnOutcome {
        require(pack.parameters.mode == "scripted") { "ScriptedAgentRunner requires mode=scripted" }
        val script = item.script ?: error("scripted mode requires ask.script for turn $turnIndex")
        val profile = profileRegistry.resolve(pack.profileId)
            ?: error("unknown profileId: ${pack.profileId}")

        val events = mutableListOf<AgentEvent>()
        var lastEventType: String? = null
        val exhaustionContext = ScriptExhaustionContext(
            profileId = pack.profileId,
            turnIndex = turnIndex,
            lastEventType = null,
            scriptStepsTotal = script.size,
        )
        val queue = ScriptQueue(script)
        val model = ScriptedStreamingChatModel(
            queue = queue,
            exhaustionContext = exhaustionContext.copy(lastEventType = lastEventType),
            onStepConsumed = { },
        )
        val persistence = AgentPersistenceContext()
        val agent = LangChain4jAgent(
            model = model,
            profile = profile,
            persistenceContext = persistence,
        )
        val sseMapper = AgentEventToSseMapper(session.conversationId)
        val sseEvents = mutableListOf<ChatSseEvent>()

        val response = try {
            agent.run(item.ask, session) { event ->
                events.add(event)
                lastEventType = event.type
                sseEvents.addAll(sseMapper.map(event))
            }
        } catch (ex: ScriptExhaustedException) {
            throw ScriptExhaustedException(
                ex.context.copy(lastEventType = lastEventType),
            )
        }

        require(queue.isEmpty()) {
            "unused script steps remain (${pack.profileId} turn $turnIndex); declare all planner/executor model calls"
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

        return TurnOutcome(
            response = response,
            events = events.toList(),
            artifacts = artifacts,
            sseEvents = sseEvents.toList(),
            transcript = transcript,
        )
    }

    private fun ArtifactRecord.toSnapshot(): ArtifactSnapshot =
        ArtifactSnapshot(
            persistKind = kind,
            artifactId = artifactId,
            payload = payload,
        )
}
