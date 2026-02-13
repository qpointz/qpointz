package io.qpointz.mill.ai.scenarios.actions.checks;

import java.util.List;
import java.util.Map;

/**
 * Interface for verification checks.
 * Each check implementation handles a specific type of verification.
 */
public interface Check {
    /**
     * Gets the key that identifies this check type in the check parameters.
     * This key is used to route check parameters to the appropriate check implementation.
     *
     * @return the check key (e.g., "intent", "has", "sql-shape", "returns")
     */
    String getCheckKey();

    /**
     * Executes the check against the given result.
     *
     * @param checkParams the check parameters from the action configuration
     * @param result the result data to check against
     * @return list of check results (may contain multiple results for complex checks)
     */
    List<CheckResult> execute(Map<String, Object> checkParams, Map<String, Object> result);
}

