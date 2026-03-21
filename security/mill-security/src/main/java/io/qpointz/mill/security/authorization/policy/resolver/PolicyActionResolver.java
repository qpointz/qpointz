package io.qpointz.mill.security.authorization.policy.resolver;

import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.expression.CallNode;
import io.qpointz.mill.security.authorization.policy.expression.ExpressionNode;
import io.qpointz.mill.security.authorization.policy.expression.RawExpressionNode;
import io.qpointz.mill.security.authorization.policy.matcher.PolicyMatcher;
import io.qpointz.mill.security.authorization.policy.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class PolicyActionResolver {

    private final PolicySet policySet;
    private final Set<String> selectedPolicies;
    private final PolicyMatcher matcher;

    public PolicyActionResolver(PolicySet policySet, Set<String> selectedPolicies, PolicyMatcher matcher) {
        this.policySet = policySet;
        this.selectedPolicies = selectedPolicies;
        this.matcher = matcher;
    }

    public ResolvedActions resolve(List<String> tableName) {
        var tableAccess = AccessDecision.ALLOWED;
        var rowFilters = new ArrayList<ResolvedActions.ResolvedRowFilter>();
        ResolvedActions.ResolvedColumnAccess columnAccess = null;

        for (var policy : policySet.getPolicies()) {
            boolean isMember = selectedPolicies.contains(policy.getName());

            for (var action : policy.getActions()) {
                if (!matcher.matchesTable(action.getTable(), tableName)) {
                    continue;
                }

                switch (action.getType()) {
                    case ActionType.TABLE_ACCESS -> {
                        if (isMember && action.getVerb() == ActionVerb.DENY) {
                            tableAccess = AccessDecision.DENIED;
                        }
                        if (!isMember && action.getVerb() == ActionVerb.ALLOW) {
                            // non-member doesn't get ALLOW, no effect
                        }
                    }
                    case ActionType.ROW_FILTER -> {
                        if (isMember) {
                            rowFilters.add(buildRowFilter(action, false));
                        } else if (action.isExclusive()) {
                            rowFilters.add(buildRowFilter(action, true));
                        }
                    }
                    case ActionType.COLUMN_ACCESS -> {
                        if (isMember) {
                            columnAccess = buildColumnAccess(action);
                        }
                    }
                }
            }
        }

        return new ResolvedActions(tableName, tableAccess, rowFilters, columnAccess);
    }

    public PolicyEvaluationResult evaluate(List<String> groups, List<String> tableName, List<String> requestedColumns) {
        var memberPolicies = policySet.getPolicies().stream()
                .filter(p -> groups.contains(p.getName()))
                .map(Policy::getName)
                .collect(Collectors.toSet());

        var evalResolver = new PolicyActionResolver(policySet, memberPolicies, matcher);
        var resolved = evalResolver.resolve(tableName);

        var rowFilterResults = new ArrayList<PolicyEvaluationResult.RowFilterResult>();
        for (var policy : policySet.getPolicies()) {
            boolean isMember = memberPolicies.contains(policy.getName());
            for (var action : policy.getActions()) {
                if (!ActionType.ROW_FILTER.equals(action.getType())) continue;
                if (!matcher.matchesTable(action.getTable(), tableName)) continue;
                if (isMember || action.isExclusive()) {
                    rowFilterResults.add(new PolicyEvaluationResult.RowFilterResult(
                            policy.getName(),
                            action.getVerb(),
                            action.getExpression(),
                            action.getRawExpression()
                    ));
                }
            }
        }

        var columnResults = new ArrayList<PolicyEvaluationResult.ColumnResult>();
        if (requestedColumns != null) {
            for (var col : requestedColumns) {
                var colResult = resolveColumnAccess(col, memberPolicies, tableName);
                columnResults.add(colResult);
            }
        }

        var tableResult = new PolicyEvaluationResult.TableResult(
                tableName,
                resolved.tableAccess(),
                rowFilterResults,
                columnResults
        );

        return new PolicyEvaluationResult(List.of(tableResult));
    }

    private PolicyEvaluationResult.ColumnResult resolveColumnAccess(String columnName, Set<String> memberPolicies, List<String> tableName) {
        for (var policy : policySet.getPolicies()) {
            if (!memberPolicies.contains(policy.getName())) continue;
            for (var action : policy.getActions()) {
                if (!ActionType.COLUMN_ACCESS.equals(action.getType())) continue;
                if (!matcher.matchesTable(action.getTable(), tableName)) continue;
                if (!action.hasColumns()) continue;

                if (action.getColumnsMode() == ColumnsMode.EXCLUDE) {
                    for (var pattern : action.getColumns()) {
                        if (matcher.matchesColumn(pattern, columnName)) {
                            return new PolicyEvaluationResult.ColumnResult(columnName, AccessDecision.DENIED, policy.getName());
                        }
                    }
                } else if (action.getColumnsMode() == ColumnsMode.INCLUDE) {
                    boolean found = false;
                    for (var pattern : action.getColumns()) {
                        if (matcher.matchesColumn(pattern, columnName)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return new PolicyEvaluationResult.ColumnResult(columnName, AccessDecision.DENIED, policy.getName());
                    }
                }
            }
        }
        return new PolicyEvaluationResult.ColumnResult(columnName, AccessDecision.ALLOWED, null);
    }

    private ResolvedActions.ResolvedRowFilter buildRowFilter(PolicyActionEntry action, boolean negate) {
        ExpressionNode expr = action.getExpression();
        String rawExpr = action.getRawExpression();

        if (negate && expr != null) {
            expr = CallNode.not(expr);
        }
        if (negate && rawExpr != null && !rawExpr.isBlank()) {
            rawExpr = "NOT (" + rawExpr + ")";
        }

        return new ResolvedActions.ResolvedRowFilter(
                action.getVerb(),
                expr,
                rawExpr,
                negate
        );
    }

    private ResolvedActions.ResolvedColumnAccess buildColumnAccess(PolicyActionEntry action) {
        return new ResolvedActions.ResolvedColumnAccess(
                action.getColumnsMode(),
                action.getColumns() != null ? List.copyOf(action.getColumns()) : List.of()
        );
    }
}
