package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.test.scenario.v3.AskRunItem
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome

/**
 * Executes a single scenario `ask` turn and returns a collected [TurnOutcome].
 *
 * Implementations: [ScriptedAgentRunner] (deterministic script replay),
 * [ProvidedAgentRunner] (caller-supplied live agent).
 */
fun interface AgentTurnRunner {

    /**
     * Runs one turn from a scenario pack.
     *
     * @param pack Parent pack (profile and mode).
     * @param item Ask turn definition.
     * @param turnIndex Zero-based turn index.
     * @param session Conversation session for multi-turn packs.
     * @return Collected outcome for checks and regression records.
     */
    fun runTurn(
        pack: ScenarioPack,
        item: AskRunItem,
        turnIndex: Int,
        session: ConversationSession,
    ): TurnOutcome
}
