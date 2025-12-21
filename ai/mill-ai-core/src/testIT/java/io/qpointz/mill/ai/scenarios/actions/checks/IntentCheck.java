package io.qpointz.mill.ai.scenarios.actions.checks;

import io.qpointz.mill.test.scenario.ActionOutcome;
import lombok.val;

import java.util.List;
import java.util.Map;

/**
 * Check that verifies the intent matches the expected value.
 */
public class IntentCheck extends AbstractCheck {

    @Override
    public String getCheckKey() {
        return "intent";
    }

    @Override
    public List<CheckResult> execute(Map<String, Object> checkParams, Map<String, Object> result) {
        val intent = checkParams.get("intent").toString();
        val resIntent = result.getOrDefault("resultIntent", "");

        return List.of(createResult(
                resIntent.equals(intent) ? ActionOutcome.OutcomeStatus.PASS : ActionOutcome.OutcomeStatus.ERROR,
                "verify-intent",
                String.format("Expected:%s Got:%s", intent, resIntent),
                checkParams
        ));
    }
}

