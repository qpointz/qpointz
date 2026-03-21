package io.qpointz.mill.security.authorization.policy.resolver;

import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.matcher.PolicyMatcher;
import io.qpointz.mill.security.authorization.policy.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PolicyEvaluationResultTest {

    private final PolicyMatcher matcher = new PolicyMatcher();

    @Test
    void evaluate_tableAllowed_noFilters() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.TABLE_ACCESS)
                                .table(List.of("S", "T")).build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.evaluate(List.of("analysts"), List.of("S", "T"), List.of("col1"));

        assertEquals(1, result.tables().size());
        assertEquals(AccessDecision.ALLOWED, result.tables().get(0).access());
        assertTrue(result.tables().get(0).rowFilters().isEmpty());
    }

    @Test
    void evaluate_tableDenied() {
        var policies = PolicySet.of(
                Policy.builder().name("restricted").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY).type(ActionType.TABLE_ACCESS)
                                .table(List.of("HR", "SALARY")).build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("restricted"), matcher);
        var result = resolver.evaluate(List.of("restricted"), List.of("HR", "SALARY"), List.of());

        assertEquals(AccessDecision.DENIED, result.tables().get(0).access());
    }

    @Test
    void evaluate_withRowFilter() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .rawExpression("region = 'EMEA'").build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.evaluate(List.of("analysts"), List.of("S", "T"), List.of());

        assertEquals(1, result.tables().get(0).rowFilters().size());
        assertEquals("analysts", result.tables().get(0).rowFilters().get(0).policyName());
        assertEquals("region = 'EMEA'", result.tables().get(0).rowFilters().get(0).rawExpression());
    }

    @Test
    void evaluate_multipleRowFilters_differentPolicies() {
        var policies = PolicySet.of(
                Policy.builder().name("p1").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .rawExpression("a = 1").build()
                )).build(),
                Policy.builder().name("p2").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .rawExpression("b = 2").build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("p1", "p2"), matcher);
        var result = resolver.evaluate(List.of("p1", "p2"), List.of("S", "T"), List.of());

        assertEquals(2, result.tables().get(0).rowFilters().size());
    }

    @Test
    void evaluate_columnDeniedByWildcardExclude() {
        var policies = PolicySet.of(
                Policy.builder().name("compliance").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY).type(ActionType.COLUMN_ACCESS)
                                .table(List.of("SALES", "CLIENT"))
                                .columns(List.of("pii_*"))
                                .columnsMode(ColumnsMode.EXCLUDE).build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("compliance"), matcher);
        var result = resolver.evaluate(
                List.of("compliance"),
                List.of("SALES", "CLIENT"),
                List.of("client_id", "name", "pii_ssn", "region")
        );

        var columns = result.tables().get(0).columns();
        assertEquals(4, columns.size());
        assertEquals(AccessDecision.ALLOWED, columns.get(0).access());
        assertEquals(AccessDecision.ALLOWED, columns.get(1).access());
        assertEquals(AccessDecision.DENIED, columns.get(2).access());
        assertEquals("compliance", columns.get(2).policyName());
        assertEquals(AccessDecision.ALLOWED, columns.get(3).access());
    }

    @Test
    void evaluate_columnNotInIncludeList_denied() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.COLUMN_ACCESS)
                                .table(List.of("S", "T"))
                                .columns(List.of("col1", "col2"))
                                .columnsMode(ColumnsMode.INCLUDE).build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.evaluate(
                List.of("analysts"),
                List.of("S", "T"),
                List.of("col1", "col2", "col3")
        );

        var columns = result.tables().get(0).columns();
        assertEquals(AccessDecision.ALLOWED, columns.get(0).access());
        assertEquals(AccessDecision.ALLOWED, columns.get(1).access());
        assertEquals(AccessDecision.DENIED, columns.get(2).access());
    }

    @Test
    void evaluate_noMatchingPolicies_allAllowed() {
        var policies = PolicySet.of(
                Policy.builder().name("other").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY).type(ActionType.TABLE_ACCESS)
                                .table(List.of("S", "T")).build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of(), matcher);
        var result = resolver.evaluate(List.of(), List.of("S", "T"), List.of("col1"));

        assertEquals(AccessDecision.ALLOWED, result.tables().get(0).access());
        assertEquals(AccessDecision.ALLOWED, result.tables().get(0).columns().get(0).access());
    }

    @Test
    void evaluate_wildcardTablePattern() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("SALES", "*"))
                                .rawExpression("active = true").build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.evaluate(
                List.of("analysts"),
                List.of("SALES", "ORDERS"),
                List.of()
        );

        assertEquals(1, result.tables().get(0).rowFilters().size());
    }
}
