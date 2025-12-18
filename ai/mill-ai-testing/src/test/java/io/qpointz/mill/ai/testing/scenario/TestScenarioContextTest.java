package io.qpointz.mill.ai.testing.scenario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link TestScenarioContext} class.
 * Tests the concrete implementation of ScenarioContext used for testing.
 */
class TestScenarioContextTest {

    /**
     * Verifies that a TestScenarioContext can be created with the default constructor.
     * The context should be initialized with an empty results list.
     */
    @Test
    void shouldCreateContext_whenUsingDefaultConstructor() {
        // Act
        TestScenarioContext context = new TestScenarioContext();

        // Assert
        assertThat(context).isNotNull();
        assertThat(context.getResultCount()).isEqualTo(0);
        assertThat(context.getResults()).isEmpty();
        assertThat(context.getLastResult()).isNull();
    }

    /**
     * Verifies that TestScenarioContext correctly inherits behavior from ScenarioContext.
     * Adding results should work the same as the base class.
     */
    @Test
    void shouldInheritScenarioContextBehavior_whenAddingResults() {
        // Arrange
        TestScenarioContext context = new TestScenarioContext();
        Action action = new Action("do-get", java.util.Map.of());
        ActionResult result = new ActionResult(action, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));

        // Act
        context.addResult(result);

        // Assert
        assertThat(context.getResultCount()).isEqualTo(1);
        assertThat(context.getResults()).containsExactly(result);
        assertThat(context.getLastResult()).isEqualTo(result);
    }
}
