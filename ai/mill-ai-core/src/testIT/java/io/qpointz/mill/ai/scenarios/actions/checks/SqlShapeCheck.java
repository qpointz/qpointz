package io.qpointz.mill.ai.scenarios.actions.checks;

import io.qpointz.mill.ai.scenarios.actions.SqlToSqlShape;
import io.qpointz.mill.test.scenario.ActionOutcome;
import lombok.val;

import java.util.List;
import java.util.Map;

/**
 * Check that verifies SQL shape characteristics.
 */
public class SqlShapeCheck extends AbstractCheck {

    @Override
    public String getCheckKey() {
        return "sql-shape";
    }

    @Override
    public List<CheckResult> execute(Map<String, Object> checkParams, Map<String, Object> result) {
        val shape = SqlToSqlShape.extract(result.get("sql").toString());
        val shapeMap = ((Map<String, Object>) checkParams.get("sql-shape"));

        val results = shapeMap.entrySet().stream().map(entry -> switch (entry.getKey()) {
            case "has-aggregation" -> compareToCheck(shape.hasAggregation(), entry, checkParams);
            case "has-grouping" -> compareToCheck(shape.hasGrouping(), entry, checkParams);
            case "has-where" -> compareToCheck(shape.hasWhere(), entry, checkParams);
            case "has-ordering" -> compareToCheck(shape.hasOrdering(), entry, checkParams);
            case "has-limit" -> compareToCheck(shape.hasLimit(), entry, checkParams);
            case "has-subquery" -> compareToCheck(shape.hasSubquery(), entry, checkParams);
            case "has-join" -> compareToCheck(shape.hasJoin(), entry, checkParams);
            default -> createResult(
                    ActionOutcome.OutcomeStatus.WARN,
                    "verify-sql-shape-" + entry.getKey(),
                    "Unknown SqlShape check:sql-shape-" + entry.getKey(),
                    checkParams
            );
        }).toList();
        return results;
    }

    private CheckResult compareToCheck(Object got, Map.Entry<String, Object> entry, Map<String, Object> checkParams) {
        val status = entry.getValue().equals(got)
                ? ActionOutcome.OutcomeStatus.PASS
                : ActionOutcome.OutcomeStatus.ERROR;

        return createResult(
                status,
                "verify-sql-shape-" + entry.getKey(),
                String.format("Expected: %s Got: %s", entry.getValue().toString(), got.toString()),
                checkParams
        );
    }
}

