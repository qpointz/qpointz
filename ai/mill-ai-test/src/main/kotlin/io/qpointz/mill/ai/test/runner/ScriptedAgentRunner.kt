package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.profile.PlatformProfiles
import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.runtime.langchain4j.LangChain4jAgent
import io.qpointz.mill.ai.test.scenario.v3.AskRunItem
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome

/**
 * Runs a single scenario turn using a scripted model and real [LangChain4jAgent] handlers.
 */
class ScriptedAgentRunner(
    private val profileRegistry: ProfileRegistry = PlatformProfiles.registry(),
) : AgentTurnRunner {

    override fun runTurn(
        pack: ScenarioPack,
        item: AskRunItem,
        turnIndex: Int,
        session: ConversationSession,
    ): TurnOutcome {
        val script = item.script ?: error("ScriptedAgentRunner requires ask.script for turn $turnIndex")
        val profile = profileRegistry.resolve(pack.profileId)
            ?: error("unknown profileId: ${pack.profileId}")
        val context = ScenarioHarnessSupport.agentContext(profile)

        var lastEventType: String? = null
        val exhaustionContext = ScriptExhaustionContext(
            profileId = pack.profileId,
            turnIndex = turnIndex,
            lastEventType = null,
            scriptStepsTotal = script.size,
        )
        val logContext = turnLogContext(pack, turnIndex, session)
        val queue = ScriptQueue(script)
        var scriptInvocation = 0
        val model = ScriptedStreamingChatModel(
            queue = queue,
            exhaustionContext = exhaustionContext,
            onStepConsumed = { step ->
                ScenarioActivityLogger.logScriptModelStep(logContext, scriptInvocation++, step)
            },
        )
        val persistence = AgentPersistenceContext()
        val agent = LangChain4jAgent(
            model = model,
            profile = profile,
            persistenceContext = persistence,
        )

        return try {
            val outcome = AgentTurnSupport.collectTurnOutcome(
                agent = agent,
                persistence = persistence,
                session = session,
                ask = item.ask,
                context = context,
                logContext = logContext,
                onEvent = { lastEventType = it.type },
            )
            require(queue.isEmpty()) {
                "unused script steps remain (${pack.profileId} turn $turnIndex); declare all planner/executor model calls"
            }
            outcome
        } catch (ex: ScriptExhaustedException) {
            throw ScriptExhaustedException(
                ex.context.copy(lastEventType = lastEventType),
            )
        }
    }

    private fun turnLogContext(
        pack: ScenarioPack,
        turnIndex: Int,
        session: ConversationSession,
    ): ScenarioActivityLogger.TurnContext =
        ScenarioActivityLogger.TurnContext(
            packName = pack.name,
            profileId = pack.profileId,
            mode = pack.parameters.mode,
            turnIndex = turnIndex,
            runnerKind = "scripted",
            conversationId = session.conversationId,
        )
}
