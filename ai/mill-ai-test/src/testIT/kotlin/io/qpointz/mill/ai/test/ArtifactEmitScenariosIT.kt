package io.qpointz.mill.ai.test

import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPackTestBase

/**
 * Primary acceptance for artefact emit contract (WI-307): SQL and facet POC scenario packs.
 */
class ArtifactEmitScenariosIT : ScenarioPackTestBase() {

    override fun scenarioResources(): List<String> = listOf(
        "scenarios/artifact-emit/data-analysis-sql-emit.yml",
        "scenarios/artifact-emit/schema-authoring-facet-emit.yml",
        "scenarios/artifact-emit/data-analysis-no-facet.yml",
        "scenarios/artifact-emit/schema-authoring-sql-and-facet.yml",
    )

    override fun baselineResourceFor(pack: ScenarioPack): String =
        "scenarios/baselines/${pack.slug()}.record.normalized.json"
}
