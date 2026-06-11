package io.qpointz.mill.ai.test.scenario.v3

import io.qpointz.mill.ai.test.runner.ScenarioPackRunner
import io.qpointz.mill.ai.test.scenario.v3.ConversationRegressionComparator
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPackLoader
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.file.Files
import java.util.stream.Stream

/**
 * JUnit 5 base for YAML scenario pack integration tests.
 *
 * Subclasses declare classpath scenario resources; each pack becomes a dynamic test.
 * Override [createPackRunner] to inject a different [io.qpointz.mill.ai.test.runner.AgentTurnRunner]
 * (e.g. a live agent via [io.qpointz.mill.ai.test.runner.ProvidedAgentRunner] in testIT).
 */
abstract class ScenarioPackTestBase {

    /**
     * Classpath paths to scenario YAML files (under `src/testIT/resources` or `src/test/resources`).
     */
    protected abstract fun scenarioResources(): List<String>

    /**
     * Creates the pack runner for a loaded scenario. Default: [ScenarioPackRunner.scripted].
     *
     * @param pack Loaded pack (profile and mode available for agent construction).
     */
    protected open fun createPackRunner(pack: ScenarioPack): ScenarioPackRunner =
        ScenarioPackRunner.scripted()

    /**
     * Optional metadata merged into the regression record (e.g. `modelName` for live runs).
     */
    protected open fun runMetaExtras(pack: ScenarioPack): Map<String, Any?> = emptyMap()

    /**
     * Optional committed baseline classpath path for regression diff (`scenarios/baselines/...`).
     * Return null to skip baseline comparison.
     */
    protected open fun baselineResourceFor(pack: ScenarioPack): String? = null

    /**
     * Creates one dynamic test per scenario resource.
     */
    @TestFactory
    fun scenarioPacks(): Stream<DynamicTest> =
        scenarioResources().stream().map { resource ->
            DynamicTest.dynamicTest(resource) {
                val pack = ScenarioPackLoader.fromClasspath(resource, classLoader())
                val result = createPackRunner(pack).run(pack, resource, runMetaExtras(pack))
                assertTrue(
                    result.passed,
                    { "Scenario $resource failed: ${result.failures}" },
                )
                baselineResourceFor(pack)?.let { baselineResource ->
                    val baselinePath = BaselineResourcePaths.resolve(baselineResource, classLoader())
                    val normalizedJson = Files.readString(result.paths.normalized)
                    ConversationRegressionComparator().updateOrAssert(
                        normalizedJson,
                        baselinePath,
                    )
                }
            }
        }

    /**
     * Classloader for scenario resources; override when packs live in testIT resources.
     */
    protected open fun classLoader(): ClassLoader = javaClass.classLoader
}
