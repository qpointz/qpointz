package io.qpointz.mill.ai.testing.scenario;

/**
 * Test implementation of ScenarioRunner that returns PASS outcome for all actions.
 * Useful for testing the scenario execution framework without implementing actual action logic.
 */
public class TestScenarioRunner extends ScenarioRunner<TestScenarioContext, ActionResult> {

    /**
     * Creates a new test scenario runner with a new context.
     */
    public TestScenarioRunner() {
        super(new TestScenarioContext());
    }

    /**
     * Creates a new test scenario runner with the given context.
     *
     * @param context the test scenario context to use
     */
    public TestScenarioRunner(TestScenarioContext context) {
        super(context);
    }

    /**
     * Executes an action and always returns a PASS outcome.
     * This is a test implementation that doesn't perform any actual action execution.
     *
     * @param context the scenario context
     * @param action the action to execute
     * @return a PASS ActionOutcome
     */
    @Override
    protected ActionOutcome executeAction(TestScenarioContext context, Action action) {
        return ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS);
    }
}
