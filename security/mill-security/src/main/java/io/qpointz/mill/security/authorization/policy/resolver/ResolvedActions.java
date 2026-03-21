package io.qpointz.mill.security.authorization.policy.resolver;

import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.expression.ExpressionNode;
import io.qpointz.mill.security.authorization.policy.model.AccessDecision;
import io.qpointz.mill.security.authorization.policy.model.ColumnsMode;

import java.util.List;

public record ResolvedActions(
        List<String> table,
        AccessDecision tableAccess,
        List<ResolvedRowFilter> rowFilters,
        ResolvedColumnAccess columnAccess
) {

    public record ResolvedRowFilter(
            ActionVerb verb,
            ExpressionNode expression,
            String rawExpression,
            boolean negated
    ) {}

    public record ResolvedColumnAccess(
            ColumnsMode mode,
            List<String> columns
    ) {}

    public boolean isDenied() {
        return tableAccess == AccessDecision.DENIED;
    }

    public boolean hasRowFilters() {
        return rowFilters != null && !rowFilters.isEmpty();
    }

    public boolean hasColumnRestrictions() {
        return columnAccess != null;
    }
}
