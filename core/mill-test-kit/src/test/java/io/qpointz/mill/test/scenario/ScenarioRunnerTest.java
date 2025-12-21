package io.qpointz.mill.test.scenario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link ScenarioRunner} abstract class.
 * Tests scenario execution, success calculation, error handling, and metrics recording.
 */
class ScenarioRunnerTest {

    private TestScenarioContext context;
    private TestScenarioRunner runner;

    @BeforeEach
    void setUp() {
        context = new TestScenarioContext();
        runner = new TestScenarioRunner(context);
    }

    /**
     * Verifies that all actions in a scenario are executed sequentially.
     * Each action should produce a result that is added to the context.
     */
    @Test
    void shouldExecuteAllActions_whenRunningScenario() {
        // Arrange
        Action action1 = new Action("do-get", Map.of());
        Action action2 = new Action("do-ask", Map.of());
        Scenario scenario = new Scenario("test", List.of(action1, action2));

        // Act
        TestScenarioContext result = runner.run(scenario);

        // Assert
        assertThat(result).isSameAs(context);
        assertThat(result.getResultCount()).isEqualTo(2);
        assertThat(result.getResults().get(0).action()).isEqualTo(action1);
        assertThat(result.getResults().get(1).action()).isEqualTo(action2);
    }

    /**
     * Verifies that success is calculated correctly when the outcome level meets or exceeds
     * the required pass level. PASS (level 2) should pass when WARN (level 1) is required.
     */
    @Test
    void shouldCalculateSuccess_whenOutcomeLevelMeetsPassLevel() {
        // Arrange
        Action action = new Action("check-has-sql", Map.of("pass", "WARN"));
        Scenario scenario = new Scenario("test", List.of(action));

        // Act - TestScenarioRunner returns PASS, which should pass WARN level
        runner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.success()).isTrue(); // PASS (2) >= WARN (1)
    }

    /**
     * Verifies that success is false when the outcome level is below the required pass level.
     * WARN (level 1) should fail when PASS (level 2) is required.
     */
    @Test
    void shouldCalculateFailure_whenOutcomeLevelBelowPassLevel() {
        // Arrange
        Action action = new Action("check-has-sql", Map.of("pass", "PASS"));
        Scenario scenario = new Scenario("test", List.of(action));
        LowLevelOutcomeRunner lowLevelRunner = new LowLevelOutcomeRunner(context,
                ActionOutcome.of(ActionOutcome.OutcomeStatus.WARN));

        // Act
        lowLevelRunner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.success()).isFalse(); // WARN (1) < PASS (2)
    }

    /**
     * Verifies that when no pass level is specified in action params, the default
     * WARN level (1) is used. This provides sensible defaults for actions.
     */
    @Test
    void shouldUseDefaultWarnLevel_whenPassLevelNotSpecified() {
        // Arrange
        Action action = new Action("check-has-sql", Map.of());
        Scenario scenario = new Scenario("test", List.of(action));
        LowLevelOutcomeRunner lowLevelRunner = new LowLevelOutcomeRunner(context,
                ActionOutcome.of(ActionOutcome.OutcomeStatus.WARN));

        // Act
        lowLevelRunner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.success()).isTrue(); // WARN (1) >= WARN (1) default
    }

    /**
     * Verifies that the pass level can be specified as a string (e.g., "ERROR", "WARN", "PASS").
     * The string is parsed to the corresponding numeric level.
     */
    @Test
    void shouldParsePassLevelFromString_whenPassIsString() {
        // Arrange
        Action action = new Action("check-has-sql", Map.of("pass", "ERROR"));
        Scenario scenario = new Scenario("test", List.of(action));
        LowLevelOutcomeRunner lowLevelRunner = new LowLevelOutcomeRunner(context,
                ActionOutcome.of(ActionOutcome.OutcomeStatus.ERROR));

        // Act
        lowLevelRunner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.success()).isTrue(); // ERROR (0) >= ERROR (0)
    }

    /**
     * Verifies that the pass level can be specified as a number (0, 1, or 2).
     * This provides a direct way to specify the required level.
     */
    @Test
    void shouldParsePassLevelFromNumber_whenPassIsNumber() {
        // Arrange
        Action action = new Action("check-has-sql", Map.of("pass", 2));
        Scenario scenario = new Scenario("test", List.of(action));
        LowLevelOutcomeRunner lowLevelRunner = new LowLevelOutcomeRunner(context,
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));

        // Act
        lowLevelRunner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.success()).isTrue(); // PASS (2) >= PASS (2)
    }

    /**
     * Verifies that when pass level is null, the default WARN level is used.
     * This ensures null values don't cause errors.
     */
    @Test
    void shouldUseDefaultWarnLevel_whenPassLevelIsNull() {
        // Arrange
        Action action = new Action("check-has-sql", Map.of());
        Scenario scenario = new Scenario("test", List.of(action));
        LowLevelOutcomeRunner lowLevelRunner = new LowLevelOutcomeRunner(context,
                ActionOutcome.of(ActionOutcome.OutcomeStatus.WARN));

        // Act
        lowLevelRunner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.success()).isTrue(); // WARN (1) >= WARN (1) default
    }

    /**
     * Verifies that when pass level is an invalid type (not string or number),
     * the default WARN level is used. This provides graceful degradation.
     */
    @Test
    void shouldUseDefaultWarnLevel_whenPassLevelIsInvalidType() {
        // Arrange
        Action action = new Action("check-has-sql", Map.of("pass", new Object()));
        Scenario scenario = new Scenario("test", List.of(action));
        LowLevelOutcomeRunner lowLevelRunner = new LowLevelOutcomeRunner(context,
                ActionOutcome.of(ActionOutcome.OutcomeStatus.WARN));

        // Act
        lowLevelRunner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.success()).isTrue(); // WARN (1) >= WARN (1) default
    }

    /**
     * Verifies that when an action execution throws an exception, the runner:
     * 1. Catches the exception
     * 2. Creates a failed result with ERROR status
     * 3. Records the error message and stacktrace
     * 4. Adds error metrics
     */
    @Test
    void shouldHandleException_whenActionExecutionThrows() {
        // Arrange
        Action action = new Action("do-get", Map.of());
        Scenario scenario = new Scenario("test", List.of(action));
        ExceptionThrowingRunner exceptionRunner = new ExceptionThrowingRunner(context);

        // Act
        TestScenarioContext result = exceptionRunner.run(scenario);

        // Assert
        assertThat(result.getResultCount()).isEqualTo(1);
        ActionResult failedResult = result.getResults().get(0);
        assertThat(failedResult.success()).isFalse();
        assertThat(failedResult.errorMessage()).isPresent();
        assertThat(failedResult.errorMessage().get()).contains("Test exception");
        assertThat(failedResult.outcome().status()).isEqualTo(ActionOutcome.OutcomeStatus.ERROR);
    }

    /**
     * Verifies that execution time is automatically recorded as a metric
     * when an action succeeds. This provides performance tracking.
     */
    @Test
    void shouldAddExecutionTimeMetric_whenActionSucceeds() {
        // Arrange
        Action action = new Action("do-get", Map.of());
        Scenario scenario = new Scenario("test", List.of(action));

        // Act
        runner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.outcome().metrics()).isPresent();
        assertThat(result.outcome().metrics().get()).containsKey("execution.time");
    }

    /**
     * Verifies that both execution time and error information are recorded as metrics
     * when an action fails. This provides debugging information for failed actions.
     */
    @Test
    void shouldAddExecutionTimeAndErrorMetric_whenActionFails() {
        // Arrange
        Action action = new Action("do-get", Map.of());
        Scenario scenario = new Scenario("test", List.of(action));
        ExceptionThrowingRunner exceptionRunner = new ExceptionThrowingRunner(context);

        // Act
        exceptionRunner.run(scenario);

        // Assert
        ActionResult result = context.getResults().get(0);
        assertThat(result.outcome().metrics()).isPresent();
        assertThat(result.outcome().metrics().get()).containsKey("execution.time");
        assertThat(result.outcome().metrics().get()).containsKey("error");
    }

    /**
     * Verifies that running a null scenario throws an IllegalArgumentException.
     * This prevents null pointer exceptions and provides clear error messages.
     */
    @Test
    void shouldThrowException_whenScenarioIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> runner.run(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scenario cannot be null");
    }

    /**
     * Verifies that creating a runner with a null context throws an IllegalArgumentException.
     * The context is required to store execution results.
     */
    @Test
    void shouldThrowException_whenContextIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new TestScenarioRunner(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ScenarioContext cannot be null");
    }

    /**
     * Test runner that throws an exception for testing error handling.
     */
    private static class ExceptionThrowingRunner extends ScenarioRunner<TestScenarioContext, ActionResult> {
        ExceptionThrowingRunner(TestScenarioContext context) {
            super(context);
        }

        @Override
        protected ActionOutcome executeAction(TestScenarioContext context, Action action) throws Exception {
            throw new RuntimeException("Test exception");
        }
    }

    /**
     * Test runner that returns a specific outcome for testing success calculation.
     */
    private static class LowLevelOutcomeRunner extends ScenarioRunner<TestScenarioContext, ActionResult> {
        private final ActionOutcome outcome;

        LowLevelOutcomeRunner(TestScenarioContext context, ActionOutcome outcome) {
            super(context);
            this.outcome = outcome;
        }

        @Override
        protected ActionOutcome executeAction(TestScenarioContext context, Action action) throws Exception {
            return outcome;
        }
    }
}
