package io.qpointz.mill.ai.test.scenario.v3

import io.qpointz.mill.ai.test.runner.ScenarioPackRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

class ScenarioBaselineStabilityTest {

    @Test
    fun shouldProduceIdenticalNormalizedJson_onDuplicateScriptedRun() {
        val resource = "scenarios/artifact-emit/data-analysis-sql-emit.yml"
        val loader = ScenarioBaselineStabilityTest::class.java.classLoader
        val pack = ScenarioPackLoader.fromClasspath(resource, loader)
        val runner = ScenarioPackRunner.scripted()
        val first = runner.run(pack, resource)
        val second = runner.run(pack, resource)
        val normalizedFirst = Files.readString(first.paths.normalized)
        val normalizedSecond = Files.readString(second.paths.normalized)
        assertThat(normalizedFirst).isEqualTo(normalizedSecond)
    }
}
