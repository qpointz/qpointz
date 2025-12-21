package io.qpointz.mill.ai.scenarios.actions.checks;

import io.qpointz.mill.test.scenario.ActionOutcome;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Check that verifies the result contains specific keys.
 */
public class HasCheck extends AbstractCheck {

    @Override
    public String getCheckKey() {
        return "has";
    }

    @Override
    public List<CheckResult> execute(Map<String, Object> checkParams, Map<String, Object> result) {
        val keys = (ArrayList<String>) checkParams.getOrDefault("has", List.of());
        return keys.stream().map(key -> {
                    val hasKey = result.containsKey(key);
                    return createResult(
                            hasKey ? ActionOutcome.OutcomeStatus.PASS : ActionOutcome.OutcomeStatus.ERROR,
                            String.format("verify-has-%s", key),
                            "Result should contain:" + key.toString(),
                            checkParams
                    );
                })
                .toList();
    }
}

