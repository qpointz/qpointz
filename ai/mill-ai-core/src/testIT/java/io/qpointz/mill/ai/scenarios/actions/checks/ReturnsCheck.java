package io.qpointz.mill.ai.scenarios.actions.checks;

import io.qpointz.mill.ai.nlsql.processors.ExecuteQueryProcessor;
import io.qpointz.mill.ai.nlsql.processors.QueryResult;
import io.qpointz.mill.ai.nlsql.processors.SubmitQueryProcessor;
import io.qpointz.mill.test.scenario.ActionOutcome;
import lombok.val;

import java.util.List;
import java.util.Map;

/**
 * Check that verifies the number of records returned.
 */
public class ReturnsCheck extends AbstractCheck {

    @Override
    public String getCheckKey() {
        return "returns";
    }

    @Override
    public List<CheckResult> execute(Map<String, Object> checkParams, Map<String, Object> result) {
        if (!result.containsKey("data")) {
            return List.of(createResult(
                    ActionOutcome.OutcomeStatus.WARN,
                    "verify-returns",
                    "No data present",
                    checkParams
            ));
        }

        val qr = ((QueryResult) result.get("data")).container();
        var records = -1;
        if (qr instanceof SubmitQueryProcessor.PagingResult) {
            val pr = (SubmitQueryProcessor.PagingResult) qr;
            records = pr.data().size();
        } else if (qr instanceof ExecuteQueryProcessor.ExecutionResult) {
            val pr = (ExecuteQueryProcessor.ExecutionResult) qr;
            records = pr.data().size();
        }

        val expect = checkParams.get("returns").toString();

        if ("not-empty".equals(expect)) {
            return List.of(createResult(
                    records > 0 ? ActionOutcome.OutcomeStatus.PASS : ActionOutcome.OutcomeStatus.ERROR,
                    "verify-returns-not-empty",
                    "Expected not empty result",
                    checkParams
            ));
        }

        if ("empty".equals(expect)) {
            return List.of(createResult(
                    records == 0 ? ActionOutcome.OutcomeStatus.PASS : ActionOutcome.OutcomeStatus.ERROR,
                    "verify-returns-empty",
                    "Expected empty result",
                    checkParams
            ));
        }

        try {
            val rcs = Integer.parseInt(expect);
            return List.of(createResult(
                    records == rcs ? ActionOutcome.OutcomeStatus.PASS : ActionOutcome.OutcomeStatus.ERROR,
                    "verify-returns-exactly",
                    String.format("Result to contain exactly %s records", rcs),
                    checkParams
            ));
        } catch (Exception ex) {
            return List.of(createResult(
                    ActionOutcome.OutcomeStatus.ERROR,
                    "verify-returns-unknown",
                    String.format("Unknown expectation: %s", expect),
                    checkParams
            ));
        }
    }
}

