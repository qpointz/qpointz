package io.qpointz.mill.ai.testing.scenario;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link ScenarioTestBase} class.
 * Tests the base functionality for running scenarios as JUnit parameterized tests.
 */
class ScenarioTestBaseTest {

    /**
     * Verifies that a concrete implementation of ScenarioTestBase can be created
     * and provides actions for parameterized tests.
     */
    @Test
    void shouldProvideActions_whenScenarioHasActions() {
        // Arrange
        ConcreteScenarioTest test = new ConcreteScenarioTest();

        // Act
        var actions = test.provideActions().toList();

        // Assert
        assertThat(actions).hasSize(3);
        assertThat(actions.get(0).get()[0]).isInstanceOf(Action.class);
        assertThat(actions.get(1).get()[0]).isInstanceOf(Action.class);
        assertThat(actions.get(2).get()[0]).isInstanceOf(Action.class);
    }

    /**
     * Verifies that display names are generated correctly for actions.
     * When name is missing, should use friendly params representation.
     */
    @Test
    void shouldGenerateDisplayNames_whenActionsProvided() {
        // Arrange
        ConcreteScenarioTest test = new ConcreteScenarioTest();

        // Act
        var actions = test.provideActions().toList();

        // Assert
        String displayName1 = (String) actions.get(0).get()[1];
        String displayName2 = (String) actions.get(1).get()[1];
        
        assertThat(displayName1).contains("test-scenario");
        assertThat(displayName1).contains("do-get");
        assertThat(displayName1).contains("message=-1"); // friendly params
        assertThat(displayName2).contains("test-scenario");
        assertThat(displayName2).contains("do-ask");
        assertThat(displayName2).contains("(question)"); // friendly params for single "value" key
    }

    /**
     * Verifies that display names use the action name when present.
     */
    @Test
    void shouldUseActionName_whenNameIsPresent() {
        // Arrange
        ConcreteScenarioTestWithNames test = new ConcreteScenarioTestWithNames();

        // Act
        var actions = test.provideActions().toList();

        // Assert
        String displayName1 = (String) actions.get(0).get()[1];
        String displayName2 = (String) actions.get(1).get()[1];
        
        assertThat(displayName1).contains("test-scenario");
        assertThat(displayName1).contains("Get message"); // uses name, not key
        assertThat(displayName1).doesNotContain("do-get");
        assertThat(displayName2).contains("test-scenario");
        assertThat(displayName2).contains("Ask question"); // uses name, not key
        assertThat(displayName2).doesNotContain("do-ask");
    }

    /**
     * Concrete implementation of ScenarioTestBase for testing.
     */
    private static class ConcreteScenarioTest extends ScenarioTestBase<TestScenarioContext, ActionResult> {
        
        @Override
        protected ScenarioRunner<TestScenarioContext, ActionResult> createRunner(TestScenarioContext context) {
            return new TestScenarioRunner(context);
        }

        @Override
        protected TestScenarioContext createContext(Scenario scenario) {
            return new TestScenarioContext();
        }

        @Override
        protected Scenario getScenario() {
            Action action1 = new Action("do-get", Map.of("message", -1));
            Action action2 = new Action("do-ask", Map.of("value", "question"));
            Action action3 = new Action("check-has-sql", Map.of("pass", "WARN"));
            return new Scenario("test-scenario", Map.of(), List.of(action1, action2, action3));
        }
    }

    /**
     * Concrete implementation of ScenarioTestBase with named actions for testing.
     */
    private static class ConcreteScenarioTestWithNames extends ScenarioTestBase<TestScenarioContext, ActionResult> {
        
        @Override
        protected ScenarioRunner<TestScenarioContext, ActionResult> createRunner(TestScenarioContext context) {
            return new TestScenarioRunner(context);
        }

        @Override
        protected TestScenarioContext createContext(Scenario scenario) {
            return new TestScenarioContext();
        }

        @Override
        protected Scenario getScenario() {
            Action action1 = new Action("do-get", java.util.Optional.of("Get message"), Map.of("message", -1));
            Action action2 = new Action("do-ask", java.util.Optional.of("Ask question"), Map.of("value", "question"));
            return new Scenario("test-scenario", Map.of(), List.of(action1, action2));
        }
    }
}
