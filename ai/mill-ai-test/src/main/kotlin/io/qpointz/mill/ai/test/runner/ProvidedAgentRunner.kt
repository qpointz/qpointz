package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.runtime.langchain4j.LangChain4jAgent
import io.qpointz.mill.ai.test.scenario.v3.AskRunItem
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome

/**
 * Runs scenario turns using a caller-supplied [LangChain4jAgent] and persistence context.
 *
 * Intended for live testIT runs where the test constructs the agent (e.g. via
 * [LangChain4jAgent.fromEnv]) and reuses it across all turns in a pack. Any `ask.script`
 * entries in the YAML are ignored.
 */
class ProvidedAgentRunner(
    private val agent: LangChain4jAgent,
    private val persistence: AgentPersistenceContext,
) : AgentTurnRunner {

    override fun runTurn(
        pack: ScenarioPack,
        item: AskRunItem,
        turnIndex: Int,
        session: ConversationSession,
    ): TurnOutcome =
        AgentTurnSupport.collectTurnOutcome(
            agent = agent,
            persistence = persistence,
            session = session,
            ask = item.ask,
            logContext = ScenarioActivityLogger.TurnContext(
                packName = pack.name,
                profileId = pack.profileId,
                mode = pack.parameters.mode,
                turnIndex = turnIndex,
                runnerKind = "provided",
                conversationId = session.conversationId,
            ),
        )
}
