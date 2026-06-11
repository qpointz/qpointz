package io.qpointz.mill.ai.test

import io.qpointz.mill.ai.persistence.AgentPersistenceContext
import io.qpointz.mill.ai.profile.DefaultProfileRegistry
import io.qpointz.mill.ai.runtime.langchain4j.LangChain4jAgent
import io.qpointz.mill.ai.test.runner.ProvidedAgentRunner
import io.qpointz.mill.ai.test.runner.ScenarioPackRunner
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPackTestBase
import org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * Base for live LLM scenario packs (`parameters.mode: live`).
 *
 * Constructs a real [LangChain4jAgent] per pack via [LangChain4jAgent.fromEnv] and injects it
 * through [ProvidedAgentRunner]. Skips when `OPENAI_API_KEY` is absent.
 */
abstract class LiveScenarioPackTestBase : ScenarioPackTestBase() {

    override fun createPackRunner(pack: ScenarioPack): ScenarioPackRunner {
        assumeTrue(
            !System.getenv("OPENAI_API_KEY").isNullOrBlank(),
            "OPENAI_API_KEY must be set for live scenario ITs",
        )
        require(pack.parameters.mode == "live") {
            "live scenario base requires parameters.mode: live (pack=${pack.name})"
        }
        val profile = DefaultProfileRegistry.resolve(pack.profileId)
            ?: error("unknown profileId: ${pack.profileId}")
        val persistence = AgentPersistenceContext()
        val agent = requireNotNull(LangChain4jAgent.fromEnv(profile, persistenceContext = persistence)) {
            "LangChain4jAgent.fromEnv returned null despite OPENAI_API_KEY being set"
        }
        return ScenarioPackRunner(ProvidedAgentRunner(agent, persistence))
    }

    override fun runMetaExtras(pack: ScenarioPack): Map<String, Any?> =
        mapOf(
            "modelName" to (System.getenv("OPENAI_MODEL")?.takeIf { it.isNotBlank() } ?: "gpt-4o-mini"),
        )
}
