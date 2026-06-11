package io.qpointz.mill.ai.test

import io.qpointz.mill.ai.test.scenario.v3.ScenarioPackTestBase

/**
 * Scripted scenario smoke tests for the v3 conversation harness (WI-302).
 */
class HarnessSmokeScenariosIT : ScenarioPackTestBase() {

    override fun scenarioResources(): List<String> = listOf(
        "scenarios/harness-smoke-hello.yml",
        "scenarios/harness-smoke-events.yml",
    )
}
