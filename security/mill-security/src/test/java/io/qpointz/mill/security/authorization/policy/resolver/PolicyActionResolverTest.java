package io.qpointz.mill.security.authorization.policy.resolver;

import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.expression.CallNode;
import io.qpointz.mill.security.authorization.policy.expression.FieldRefNode;
import io.qpointz.mill.security.authorization.policy.expression.LiteralNode;
import io.qpointz.mill.security.authorization.policy.matcher.PolicyMatcher;
import io.qpointz.mill.security.authorization.policy.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PolicyActionResolverTest {

    private final PolicyMatcher matcher = new PolicyMatcher();

    @Test
    void resolve_exactTableMatch_rowFilter() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.ROW_FILTER)
                                .table(List.of("ts", "clients"))
                                .rawExpression("region = 'EMEA'")
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.resolve(List.of("ts", "clients"));

        assertFalse(result.isDenied());
        assertTrue(result.hasRowFilters());
        assertEquals(1, result.rowFilters().size());
        assertEquals("region = 'EMEA'", result.rowFilters().get(0).rawExpression());
        assertFalse(result.rowFilters().get(0).negated());
    }

    @Test
    void resolve_wildcardTableMatch() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.ROW_FILTER)
                                .table(List.of("SALES", "*"))
                                .rawExpression("status = 'ACTIVE'")
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.resolve(List.of("SALES", "CLIENT"));

        assertTrue(result.hasRowFilters());
        assertEquals("status = 'ACTIVE'", result.rowFilters().get(0).rawExpression());
    }

    @Test
    void resolve_noMatchingActions() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.ROW_FILTER)
                                .table(List.of("HR", "SALARY"))
                                .rawExpression("x = 1")
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.resolve(List.of("SALES", "CLIENT"));

        assertFalse(result.isDenied());
        assertFalse(result.hasRowFilters());
        assertFalse(result.hasColumnRestrictions());
    }

    @Test
    void resolve_tableDenied() {
        var policies = PolicySet.of(
                Policy.builder().name("restricted").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY)
                                .type(ActionType.TABLE_ACCESS)
                                .table(List.of("HR", "SALARY"))
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("restricted"), matcher);
        var result = resolver.resolve(List.of("HR", "SALARY"));

        assertTrue(result.isDenied());
    }

    @Test
    void resolve_columnAccess_exclude() {
        var policies = PolicySet.of(
                Policy.builder().name("compliance").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY)
                                .type(ActionType.COLUMN_ACCESS)
                                .table(List.of("SALES", "CLIENT"))
                                .columns(List.of("pii_*"))
                                .columnsMode(ColumnsMode.EXCLUDE)
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("compliance"), matcher);
        var result = resolver.resolve(List.of("SALES", "CLIENT"));

        assertTrue(result.hasColumnRestrictions());
        assertEquals(ColumnsMode.EXCLUDE, result.columnAccess().mode());
        assertEquals(List.of("pii_*"), result.columnAccess().columns());
    }

    @Test
    void resolve_columnAccess_include() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.COLUMN_ACCESS)
                                .table(List.of("SCHEMA", "TABLE"))
                                .columns(List.of("col1", "col2", "col3"))
                                .columnsMode(ColumnsMode.INCLUDE)
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.resolve(List.of("SCHEMA", "TABLE"));

        assertTrue(result.hasColumnRestrictions());
        assertEquals(ColumnsMode.INCLUDE, result.columnAccess().mode());
    }

    @Test
    void resolve_multipleRowFilters_fromDifferentPolicies() {
        var policies = PolicySet.of(
                Policy.builder().name("policy1").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .rawExpression("a = 1")
                                .build()
                )).build(),
                Policy.builder().name("policy2").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .rawExpression("b = 2")
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("policy1", "policy2"), matcher);
        var result = resolver.resolve(List.of("S", "T"));

        assertEquals(2, result.rowFilters().size());
    }

    @Test
    void resolve_exclusiveRowFilter_member() {
        var expr = CallNode.eq(
                FieldRefNode.builder().fieldName("country").build(),
                LiteralNode.builder().value("US").build()
        );
        var policies = PolicySet.of(
                Policy.builder().name("us_team").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .expression(expr)
                                .rawExpression("country = 'US'")
                                .exclusive(true)
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("us_team"), matcher);
        var result = resolver.resolve(List.of("S", "T"));

        assertTrue(result.hasRowFilters());
        assertFalse(result.rowFilters().get(0).negated());
        assertEquals("country = 'US'", result.rowFilters().get(0).rawExpression());
    }

    @Test
    void resolve_exclusiveRowFilter_nonMember() {
        var expr = CallNode.eq(
                FieldRefNode.builder().fieldName("country").build(),
                LiteralNode.builder().value("US").build()
        );
        var policies = PolicySet.of(
                Policy.builder().name("us_team").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .expression(expr)
                                .rawExpression("country = 'US'")
                                .exclusive(true)
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("other_team"), matcher);
        var result = resolver.resolve(List.of("S", "T"));

        assertTrue(result.hasRowFilters());
        assertTrue(result.rowFilters().get(0).negated());
        assertEquals("NOT (country = 'US')", result.rowFilters().get(0).rawExpression());
        assertEquals("not", ((CallNode) result.rowFilters().get(0).expression()).getOperator());
    }

    @Test
    void resolve_nonExclusiveRowFilter_nonMember_notIncluded() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .rawExpression("dept = 'analytics'")
                                .exclusive(false)
                                .build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("other"), matcher);
        var result = resolver.resolve(List.of("S", "T"));

        assertFalse(result.hasRowFilters());
    }

    @Test
    void resolve_mixedActions_sameTable() {
        var policies = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.TABLE_ACCESS)
                                .table(List.of("S", "T")).build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .rawExpression("region = 'EMEA'").build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY).type(ActionType.COLUMN_ACCESS)
                                .table(List.of("S", "T"))
                                .columns(List.of("pii_*"))
                                .columnsMode(ColumnsMode.EXCLUDE).build()
                )).build()
        );

        var resolver = new PolicyActionResolver(policies, Set.of("analysts"), matcher);
        var result = resolver.resolve(List.of("S", "T"));

        assertFalse(result.isDenied());
        assertTrue(result.hasRowFilters());
        assertTrue(result.hasColumnRestrictions());
    }
}
