package io.qpointz.mill.ai.test

import io.qpointz.mill.ai.test.runner.PackRunResult
import io.qpointz.mill.ai.test.runner.ScenarioPackRunner
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack

/**
 * Runs a loaded [ScenarioPack] and returns the regression record result.
 */
fun interface ScenarioRunner {
    /**
     * Executes the pack.
     *
     * @param pack Loaded scenario pack.
     * @param scenarioSource Source label for the regression record.
     */
    fun run(pack: ScenarioPack, scenarioSource: String): PackRunResult
}

/** Default [ScenarioRunner] delegating to [ScenarioPackRunner]. */
val defaultScenarioRunner: ScenarioRunner = ScenarioRunner { pack, source ->
    ScenarioPackRunner().run(pack, source)
}
