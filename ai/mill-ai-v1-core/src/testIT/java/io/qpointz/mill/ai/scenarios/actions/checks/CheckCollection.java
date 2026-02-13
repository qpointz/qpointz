package io.qpointz.mill.ai.scenarios.actions.checks;

import io.qpointz.mill.test.scenario.ActionOutcome;

import java.util.List;

/**
 * Collection of check results with overall status.
 */
public record CheckCollection(
        ActionOutcome.OutcomeStatus status,
        List<CheckResult> results
) {
}

