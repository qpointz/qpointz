package io.qpointz.mill.ai.scenarios.actions.checks;

import io.qpointz.mill.test.scenario.ActionOutcome;

import java.util.Map;

/**
 * Result of executing a single check.
 */
public record CheckResult(
        ActionOutcome.OutcomeStatus status,
        String key,
        String message,
        Map<String, Object> params
) {
}

