package io.qpointz.mill.test.scenario;

import java.util.Optional;

/**
 * Result of executing an action.
 * Created by the runner based on the action outcome and pass level requirements.
 */
public record ActionResult(
        /**
         * The action that was executed.
         */
        Action action,

        /**
         * Whether the action execution was successful.
         * Calculated by the runner based on outcome status vs required pass level.
         */
        boolean success,

        /**
         * Optional error message if execution failed (exception occurred).
         * Includes stacktrace if available.
         */
        Optional<String> errorMessage,

        /**
         * The outcome of the action execution.
         */
        ActionOutcome outcome
) {
    public ActionResult {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("ActionOutcome cannot be null");
        }
        if (errorMessage == null) {
            errorMessage = Optional.empty();
        }
    }
}


