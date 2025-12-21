package io.qpointz.mill.test.scenario;

import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of ScenarioTestBase for testing and demonstration.
 * This shows how to use ScenarioTestBase to run scenarios as JUnit parameterized tests.
 */
public class ConcreteScenarioTest extends ScenarioTestBase<TestScenarioContext, ActionResult> {

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
        Action action2 = new Action("do-ask", Map.of("value", "question222"));
        Action action3 = new Action("check-has-sql", Map.of("pass", "WARN"));
        return new Scenario("test-scenario", Map.of(), List.of(action1, action2, action3));
    }
}
