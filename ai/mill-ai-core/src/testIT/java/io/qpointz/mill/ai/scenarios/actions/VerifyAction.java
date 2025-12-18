package io.qpointz.mill.ai.scenarios.actions;

import io.qpointz.mill.ai.nlsql.processors.ExecuteQueryProcessor;
import io.qpointz.mill.ai.nlsql.processors.QueryResult;
import io.qpointz.mill.ai.nlsql.processors.SubmitQueryProcessor;
import io.qpointz.mill.ai.scenarios.ChatAppActionExecutor;
import io.qpointz.mill.ai.scenarios.ChatAppScenarioContext;
import io.qpointz.mill.ai.testing.scenario.Action;
import io.qpointz.mill.ai.testing.scenario.ActionOutcome;
import lombok.val;

import java.util.*;

public class VerifyAction implements ChatAppActionExecutor {

    @Override
    public ActionOutcome executeAction(ChatAppScenarioContext context, Action action) throws Exception {
        val msgIdx = action.getParamAs("message", Integer.class)
                .orElse(0);
        val idx = context.getResults().size()-msgIdx-1;
        val result = context.getResults().get(idx)
                .outcome()
                .getDataAs(Map.class)
                .orElse(Map.of());

       List<CheckResult> checks = action.getParamAs("check", ArrayList.class)
               .orElse(new ArrayList())
               .stream()
               .map(z-> this.check((Map<String,Object>)z, result))
               .flatMap(k-> ((List)k).stream())
               .toList();

       val status = checks.stream()
               .map(k-> k.status)
               .min(Comparator.comparingInt(Enum::ordinal))
               .map(k-> (ActionOutcome.OutcomeStatus)k)
               .orElse(ActionOutcome.OutcomeStatus.PASS);

       val metrics = new HashMap<String,Object>();
       metrics.putAll(dataMetrics(result));
       metrics.putAll(sqlMetrics(result));

       return ActionOutcome.of(status, new CheckCollection(status, checks), metrics);
    }

    private Map<String, ?> sqlMetrics(Map result) {
        if (!result.containsKey("sql")) {
            return Map.of();
        }

        val sql = result.get("sql").toString();

        return Map.of("sql.shape", SqlToSqlShape.extract(sql));
    }

    public record CheckCollection(ActionOutcome.OutcomeStatus status, List<CheckResult> results) {}

    public record CheckResult(ActionOutcome.OutcomeStatus status, String key, String message, Map<String,Object> params) {

    }

    private List<CheckResult> check(Map<String, Object> k, Map result) {
        if (k.containsKey("intent")) {
            return checkIntent(k, result);
        }

        if (k.containsKey("has")) {
            return checkHas(k,result);
        }

        return List.of(new CheckResult(ActionOutcome.OutcomeStatus.ERROR, "verify-unknown", "Unknown check", k));
    }

    private List<CheckResult> checkIntent(Map<String, Object> k, Map result) {
        val intent = k.get("intent").toString();
        val resIntent = result.getOrDefault("resultIntent", "");

        return List.of(new CheckResult(resIntent.equals(intent) ? ActionOutcome.OutcomeStatus.PASS : ActionOutcome.OutcomeStatus.ERROR,
                "verify-intent",
                String.format("Expected:%s Got:%s", intent, resIntent),
                k));
    }

    private List<CheckResult> checkHas(Map<String, Object> k, Map result) {
        val keys = (ArrayList<String>)k.getOrDefault("has", List.of());
        return keys.stream().map(key-> {
                    val hasKey = result.containsKey(key);
                    return new CheckResult(hasKey ? ActionOutcome.OutcomeStatus.PASS : ActionOutcome.OutcomeStatus.ERROR,
                        String.format("verify-has-%s",key),
                        "Result should contain:"+key.toString(),k);
                })
                .toList();
    }

    private Map<String, ?> dataMetrics(Map result) {
        if (!result.containsKey("data")) {
            return Map.of();
        }
        val qr = ((QueryResult)result.get("data")).container();

        if (qr instanceof SubmitQueryProcessor.PagingResult) {
            val pr = (SubmitQueryProcessor.PagingResult)qr;
            return Map.of(
                    "data.container", "paging",
                    "data.fields", pr.fields(),
                    "data.size", pr.data().size()
            );
        }

        if (qr instanceof ExecuteQueryProcessor.ExecutionResult) {
            val pr = (ExecuteQueryProcessor.ExecutionResult)qr;
            return Map.of(
                    "data.container", "execute",
                    "data.fields", pr.fields(),
                    "data.size", pr.data().size()
            );
        }

        return Map.of(
                "data.container", qr.getClass().getName()
        );
    }

}
