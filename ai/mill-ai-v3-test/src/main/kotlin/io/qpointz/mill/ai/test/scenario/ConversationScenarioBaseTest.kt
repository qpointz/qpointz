package io.qpointz.mill.ai.test.scenario

import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.test.AgentScenarioResult
import io.qpointz.mill.ai.test.ScenarioRunner
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * JUnit 5 base class for YAML-driven multi-turn conversation scenario tests.
 *
 * Subclasses provide:
 * - [scenarios] — the loaded [ConversationScenario] list (typically from YAML via
 *   [ConversationScenario.fromResource])
 * - [createRunner] — a [ScenarioRunner] that drives the agent under test for a given
 *   [ConversationSession]; the session is freshly created per scenario so each scenario
 *   starts with a clean conversation context
 *
 * Each scenario becomes a [DynamicContainer]; each step becomes a [DynamicTest] named
 * after the user input. Steps share a session within a scenario but not across scenarios.
 *
 * Example:
 * ```kotlin
 * class MyAgentScenariosIT : ConversationScenarioBaseTest() {
 *     override val scenarios = ConversationScenario.fromResource("my-scenarios.yml")
 *     override fun createRunner(session: ConversationSession) = ScenarioRunner { input ->
 *         val events = mutableListOf<AgentEvent>()
 *         val response = myAgent.run(input, session, events::add)
 *         AgentScenarioResult(response, events)
 *     }
 * }
 * ```
 */
abstract class ConversationScenarioBaseTest {

    abstract val scenarios: List<ConversationScenario>

    abstract fun createRunner(session: ConversationSession): ScenarioRunner

    @TestFactory
    fun scenarioTests(): List<DynamicContainer> =
        scenarios.map { scenario ->
            val session = ConversationSession()
            val runner = createRunner(session)
            val steps = scenario.steps.mapIndexed { idx, step ->
                DynamicTest.dynamicTest("${idx + 1}. ${step.user}") {
                    val result: AgentScenarioResult = runner.run(step.user)
                    (step.expect ?: DefaultExpectations()).assert(result)
                }
            }
            DynamicContainer.dynamicContainer(scenario.name, steps)
        }
}
