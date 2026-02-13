package io.qpointz.mill.ai.scenarios.actions.checks;

import io.qpointz.mill.test.scenario.ActionOutcome;

import java.util.Map;

/**
 * Abstract base class for checks providing common utility methods.
 */
public abstract class AbstractCheck implements Check {

    /**
     * Creates a CheckResult with the given parameters.
     *
     * @param status the outcome status
     * @param key the check key
     * @param message the result message
     * @param params the original check parameters
     * @return a new CheckResult
     */
    protected CheckResult createResult(ActionOutcome.OutcomeStatus status, String key, String message, Map<String, Object> params) {
        return new CheckResult(status, key, message, params);
    }
}

