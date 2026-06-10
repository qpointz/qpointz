package io.qpointz.mill.ai.scenarios.actions;

import io.qpointz.mill.ai.nlsql.processors.ExecuteQueryProcessor;
import io.qpointz.mill.ai.nlsql.processors.QueryResult;
import io.qpointz.mill.ai.nlsql.processors.SubmitQueryProcessor;
import io.qpointz.mill.ai.scenarios.ChatAppActionExecutor;
import io.qpointz.mill.ai.scenarios.ChatAppScenarioContext;
import io.qpointz.mill.ai.scenarios.actions.checks.CheckCollection;
import io.qpointz.mill.ai.scenarios.actions.checks.CheckRegistry;
import io.qpointz.mill.ai.scenarios.actions.checks.CheckResult;
import io.qpointz.mill.test.scenario.Action;
import io.qpointz.mill.test.scenario.ActionOutcome;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;

@Slf4j
public class VerifyAction implements ChatAppActionExecutor {

    private final CheckRegistry checkRegistry;

    public VerifyAction() {
        this.checkRegistry = new CheckRegistry();
    }

    public VerifyAction(CheckRegistry checkRegistry) {
        this.checkRegistry = checkRegistry;
    }

    @Override
    public ActionOutcome executeAction(ChatAppScenarioContext context, Action action) throws Exception {
        val msgIdx = action.getParamAs("message", Integer.class)
                .orElse(0);

        val result = context.getLastResult(msgIdx)
                .outcome()
                .getDataAs(Map.class)
                .orElse(Map.of());

       log.info("Verify:{}", JsonUtils.defaultJsonMapper().writeValueAsString(result));
       List<CheckResult> checks = action.getParamAs("check", ArrayList.class)
               .orElse(new ArrayList())
               .stream()
               .map(z-> this.check((Map<String,Object>)z, result))
               .flatMap(k-> ((List)k).stream())
               .toList();

       val status = checks.stream()
               .map(k-> k.status())
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

    private List<CheckResult> check(Map<String, Object> checkParams, Map<String, Object> result) {
        // Find the check key by looking for known check parameter keys
        val checkKey = checkParams.keySet().stream()
                .filter(key -> checkRegistry.getCheck(key).isPresent())
                .findFirst()
                .orElse(null);

        if (checkKey == null) {
            return List.of(new CheckResult(
                    ActionOutcome.OutcomeStatus.ERROR,
                    "verify-unknown",
                    "Unknown check",
                    checkParams
            ));
        }

        return checkRegistry.getCheck(checkKey)
                .map(check -> check.execute(checkParams, result))
                .orElse(List.of(new CheckResult(
                        ActionOutcome.OutcomeStatus.ERROR,
                        "verify-unknown",
                        "Unknown check",
                        checkParams
                )));
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
