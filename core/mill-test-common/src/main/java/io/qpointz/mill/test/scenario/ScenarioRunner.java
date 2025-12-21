package io.qpointz.mill.test.scenario;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base class for executing test scenarios.
 * 
 * Executes actions one by one, passing them to the abstract executeAction method
 * along with the scenario context. Results are automatically appended to the context.
 * The runner is responsible for calculating success based on outcome status vs pass level.
 *
 * @param <T> the type of scenario context used
 * @param <R> the type of action result stored in the context
 */
public abstract class ScenarioRunner<T extends ScenarioContext<T, R>, R extends ActionResult> {

    private final T context;

    /**
     * Creates a new scenario runner with the given context.
     *
     * @param context the scenario context to use
     * @throws IllegalArgumentException if context is null
     */
    protected ScenarioRunner(T context) {
        if (context == null) {
            throw new IllegalArgumentException("ScenarioContext cannot be null");
        }
        this.context = context;
    }

    /**
     * Gets the scenario context.
     *
     * @return the scenario context
     */
    protected T getContext() {
        return this.context;
    }

    /**
     * Executes a test scenario by running all actions sequentially.
     *
     * @param scenario the test scenario to execute
     * @return the scenario context with all results appended
     * @throws IllegalArgumentException if scenario is null
     */
    public T run(Scenario scenario) {
        if (scenario == null) {
            throw new IllegalArgumentException("Scenario cannot be null");
        }

        for (Action action : scenario.run()) {
            R result = executeActionWithErrorHandling(action);
            this.context.addResult(result);
        }

        return this.context;
    }

    /**
     * Executes an action with error handling and creates an ActionResult.
     * If execution throws an exception, creates a failed result with error message and stacktrace.
     * Otherwise, calculates success based on outcome status vs required pass level.
     * Automatically records execution time as a metric.
     *
     * @param action the action to execute
     * @return the ActionResult with success calculated
     */
    protected R executeActionWithErrorHandling(Action action) {
        long startTime = System.nanoTime();
        try {
            ActionOutcome outcome = executeAction(this.context, action);
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            
            // Add execution time metric if not already present
            ActionOutcome outcomeWithMetrics = addExecutionTimeMetric(outcome, executionTimeMs);
            
            boolean success = calculateSuccess(action, outcomeWithMetrics);
            return createResult(action, success, Optional.empty(), outcomeWithMetrics);
        } catch (Exception e) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            String errorMessage = buildErrorMessage(e);
            ActionOutcome errorOutcome = ActionOutcome.builder(ActionOutcome.OutcomeStatus.ERROR)
                    .withMetric("execution.time", executionTimeMs)
                    .withMetric("error", e.getClass().getSimpleName())
                    .build();
            return createResult(action, false, Optional.of(errorMessage), errorOutcome);
        }
    }

    /**
     * Adds execution time metric to the outcome if not already present.
     *
     * @param outcome the original outcome
     * @param executionTimeMs the execution time in milliseconds
     * @return the outcome with execution time metric added
     */
    protected ActionOutcome addExecutionTimeMetric(ActionOutcome outcome, long executionTimeMs) {
        Map<String, Object> updatedMetrics = outcome.metrics()
                .map(m -> new java.util.HashMap<>(m))
                .orElseGet(() -> new java.util.HashMap<>());
        
        if (!updatedMetrics.containsKey("execution.time")) {
            updatedMetrics.put("execution.time", executionTimeMs);
            return new ActionOutcome(outcome.status(), outcome.data(), java.util.Optional.of(updatedMetrics));
        }
        return outcome;
    }

    /**
     * Calculates whether an action is successful based on outcome status vs required pass level.
     * 
     * The pass level is read from action.params.get("pass") and can be:
     * - "ERROR" or 0: requires ERROR level or higher
     * - "WARN" or 1: requires WARN level or higher  
     * - "PASS" or 2: requires PASS level
     * 
     * If no pass level is specified, defaults to WARN (level 1).
     * Action is successful when outcome status level >= required pass level.
     *
     * @param action the action that was executed
     * @param outcome the action outcome
     * @return true if the action is successful, false otherwise
     */
    protected boolean calculateSuccess(Action action, ActionOutcome outcome) {
        int requiredLevel = getRequiredPassLevel(action);
        int outcomeLevel = outcome.status().getLevel();
        return outcomeLevel >= requiredLevel;
    }

    /**
     * Gets the required pass level from action parameters.
     * Defaults to WARN (level 1) if not specified.
     *
     * @param action the action
     * @return the required pass level (0=ERROR, 1=WARN, 2=PASS)
     */
    protected int getRequiredPassLevel(Action action) {
        Object passObj = action.params().get("pass");
        if (passObj == null) {
            return ActionOutcome.OutcomeStatus.WARN.getLevel(); // Default to WARN
        }

        if (passObj instanceof String passStr) {
            ActionOutcome.OutcomeStatus status = ActionOutcome.OutcomeStatus.fromString(passStr);
            return status.getLevel();
        } else if (passObj instanceof Number passNum) {
            return passNum.intValue();
        } else {
            return ActionOutcome.OutcomeStatus.WARN.getLevel(); // Default to WARN
        }
    }

    /**
     * Creates an ActionResult from the action, success flag, error message, and outcome.
     * Subclasses can override this to return custom result types.
     *
     * @param action the action that was executed
     * @param success whether the action was successful
     * @param errorMessage optional error message
     * @param outcome the action outcome
     * @return the ActionResult
     */
    @SuppressWarnings("unchecked")
    protected R createResult(Action action, boolean success, Optional<String> errorMessage, ActionOutcome outcome) {
        return (R) new ActionResult(action, success, errorMessage, outcome);
    }

    /**
     * Builds an error message from an exception, including stacktrace.
     *
     * @param e the exception
     * @return the error message with stacktrace
     */
    protected String buildErrorMessage(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return e.getMessage() + "\n" + sw.toString();
    }

    /**
     * Executes a single action and returns the outcome.
     * This method must be implemented by concrete subclasses to define
     * how different action types are handled.
     *
     * @param context the scenario context
     * @param action the action to execute
     * @return the outcome of executing the action
     * @throws Exception if action execution fails
     */
    protected abstract ActionOutcome executeAction(T context, Action action) throws Exception;
}


