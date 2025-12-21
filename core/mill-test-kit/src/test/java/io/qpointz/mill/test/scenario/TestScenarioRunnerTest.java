package io.qpointz.mill.test.scenario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link TestScenarioRunner} class.
 * Tests the concrete implementation of ScenarioRunner used for testing framework functionality.
 */
class TestScenarioRunnerTest {

    private TestScenarioRunner runner;

    @BeforeEach
    void setUp() {
        runner = new TestScenarioRunner();
    }

    /**
     * Verifies that TestScenarioRunner always returns a PASS outcome for any action.
     * This makes it useful for testing the framework itself without implementing real actions.
     */
    @Test
    void shouldReturnPassOutcome_whenExecutingAction() {
        // Arrange
        Action action = new Action("do-get", Map.of());
        Scenario scenario = new Scenario("test", List.of(action));

        // Act
        TestScenarioContext result = runner.run(scenario);

        // Assert
        assertThat(result.getResultCount()).isEqualTo(1);
        ActionResult actionResult = result.getResults().get(0);
        assertThat(actionResult.outcome().status()).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(actionResult.success()).isTrue();
    }

    /**
     * Verifies that TestScenarioRunner creates a new TestScenarioContext when using
     * the default constructor. This allows standalone usage without providing a context.
     */
    @Test
    void shouldCreateNewContext_whenUsingDefaultConstructor() {
        // Act
        TestScenarioRunner newRunner = new TestScenarioRunner();
        Scenario scenario = new Scenario("test", List.of(new Action("do-get", Map.of())));

        // Assert - verify context works by running a scenario
        TestScenarioContext result = newRunner.run(scenario);
        assertThat(result).isNotNull();
        assertThat(result.getResultCount()).isEqualTo(1);
    }

    /**
     * Verifies that TestScenarioRunner uses the provided context when using
     * the context constructor. This allows sharing a context across multiple runners.
     */
    @Test
    void shouldUseProvidedContext_whenUsingContextConstructor() {
        // Arrange
        TestScenarioContext context = new TestScenarioContext();
        TestScenarioRunner newRunner = new TestScenarioRunner(context);
        Scenario scenario = new Scenario("test", List.of(new Action("do-get", Map.of())));

        // Act
        TestScenarioContext result = newRunner.run(scenario);

        // Assert - verify the same context is returned
        assertThat(result).isSameAs(context);
    }

    /**
     * Verifies that TestScenarioRunner can execute a scenario with multiple actions.
     * All actions should result in PASS outcomes and successful results.
     */
    @Test
    void shouldExecuteScenario_whenRunningWithTestRunner() {
        // Arrange
        Action action1 = new Action("do-get", Map.of());
        Action action2 = new Action("do-ask", Map.of());
        Scenario scenario = new Scenario("test", List.of(action1, action2));

        // Act
        TestScenarioContext result = runner.run(scenario);

        // Assert
        assertThat(result.getResultCount()).isEqualTo(2);
        assertThat(result.getResults().get(0).success()).isTrue();
        assertThat(result.getResults().get(1).success()).isTrue();
        assertThat(result.getResults().get(0).outcome().status()).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(result.getResults().get(1).outcome().status()).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
    }
}
