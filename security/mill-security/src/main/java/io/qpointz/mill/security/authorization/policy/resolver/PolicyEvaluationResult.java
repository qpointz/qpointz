package io.qpointz.mill.security.authorization.policy.resolver;

import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.expression.ExpressionNode;
import io.qpointz.mill.security.authorization.policy.model.AccessDecision;

import java.util.List;

public record PolicyEvaluationResult(
        List<TableResult> tables
) {

    public record TableResult(
            List<String> table,
            AccessDecision access,
            List<RowFilterResult> rowFilters,
            List<ColumnResult> columns
    ) {}

    public record RowFilterResult(
            String policyName,
            ActionVerb verb,
            ExpressionNode expression,
            String rawExpression
    ) {}

    public record ColumnResult(
            String columnName,
            AccessDecision access,
            String policyName
    ) {}
}
