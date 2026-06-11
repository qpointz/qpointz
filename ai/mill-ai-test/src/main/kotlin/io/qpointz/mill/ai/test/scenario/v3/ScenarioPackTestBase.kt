package io.qpointz.mill.ai.test.scenario.v3

import io.qpointz.mill.ai.test.runner.ScenarioPackRunner
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.stream.Stream

/**
 * JUnit 5 base for YAML scenario pack integration tests.
 *
 * Subclasses declare classpath scenario resources; each pack becomes a dynamic test.
 */
abstract class ScenarioPackTestBase {

    private val packRunner = ScenarioPackRunner()

    /**
     * Classpath paths to scenario YAML files (under `src/testIT/resources` or `src/test/resources`).
     */
    protected abstract fun scenarioResources(): List<String>

    /**
     * Creates one dynamic test per scenario resource.
     */
    @TestFactory
    fun scenarioPacks(): Stream<DynamicTest> =
        scenarioResources().stream().map { resource ->
            DynamicTest.dynamicTest(resource) {
                val pack = ScenarioPackLoader.fromClasspath(resource, classLoader())
                val result = packRunner.run(pack, resource)
                assertTrue(
                    result.passed,
                    { "Scenario $resource failed: ${result.failures}" },
                )
            }
        }

    /**
     * Classloader for scenario resources; override when packs live in testIT resources.
     */
    protected open fun classLoader(): ClassLoader = javaClass.classLoader
}
