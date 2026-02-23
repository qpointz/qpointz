package io.qpointz.mill.security.authorization.policy.model;

import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.expression.CallNode;
import io.qpointz.mill.security.authorization.policy.expression.FieldRefNode;
import io.qpointz.mill.security.authorization.policy.expression.LiteralNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyModelTest {

    @Test
    void policyActionEntry_tableAccess() {
        var entry = PolicyActionEntry.builder()
                .verb(ActionVerb.ALLOW)
                .type(ActionType.TABLE_ACCESS)
                .table(List.of("SALES", "CLIENT"))
                .build();

        assertEquals(ActionVerb.ALLOW, entry.getVerb());
        assertEquals(ActionType.TABLE_ACCESS, entry.getType());
        assertEquals(List.of("SALES", "CLIENT"), entry.getTable());
        assertFalse(entry.hasExpression());
        assertFalse(entry.hasColumns());
        assertFalse(entry.isExclusive());
    }

    @Test
    void policyActionEntry_rowFilter_rawExpression() {
        var entry = PolicyActionEntry.builder()
                .verb(ActionVerb.ALLOW)
                .type(ActionType.ROW_FILTER)
                .table(List.of("SALES", "CLIENT"))
                .rawExpression("department = 'analytics'")
                .build();

        assertTrue(entry.hasExpression());
        assertEquals("department = 'analytics'", entry.getRawExpression());
        assertNull(entry.getExpression());
    }

    @Test
    void policyActionEntry_rowFilter_structuredExpression() {
        var expr = CallNode.eq(
                FieldRefNode.builder().fieldName("department").build(),
                LiteralNode.builder().value("analytics").build()
        );
        var entry = PolicyActionEntry.builder()
                .verb(ActionVerb.ALLOW)
                .type(ActionType.ROW_FILTER)
                .table(List.of("SALES", "CLIENT"))
                .expression(expr)
                .build();

        assertTrue(entry.hasExpression());
        assertNotNull(entry.getExpression());
    }

    @Test
    void policyActionEntry_columnAccess_include() {
        var entry = PolicyActionEntry.builder()
                .verb(ActionVerb.ALLOW)
                .type(ActionType.COLUMN_ACCESS)
                .table(List.of("SCHEMA", "TABLE"))
                .columns(List.of("col1", "col2", "col3"))
                .columnsMode(ColumnsMode.INCLUDE)
                .build();

        assertTrue(entry.hasColumns());
        assertEquals(ColumnsMode.INCLUDE, entry.getColumnsMode());
        assertEquals(3, entry.getColumns().size());
    }

    @Test
    void policyActionEntry_columnAccess_exclude() {
        var entry = PolicyActionEntry.builder()
                .verb(ActionVerb.DENY)
                .type(ActionType.COLUMN_ACCESS)
                .table(List.of("SALES", "CLIENT"))
                .columns(List.of("pii_*"))
                .columnsMode(ColumnsMode.EXCLUDE)
                .build();

        assertTrue(entry.hasColumns());
        assertEquals(ColumnsMode.EXCLUDE, entry.getColumnsMode());
    }

    @Test
    void policyActionEntry_exclusive() {
        var entry = PolicyActionEntry.builder()
                .verb(ActionVerb.ALLOW)
                .type(ActionType.ROW_FILTER)
                .table(List.of("SALES", "TRANSACTIONS"))
                .rawExpression("country = 'US'")
                .exclusive(true)
                .build();

        assertTrue(entry.isExclusive());
    }

    @Test
    void policy_construction() {
        var policy = Policy.builder()
                .name("analysts")
                .actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.TABLE_ACCESS)
                                .table(List.of("SALES", "*"))
                                .build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY)
                                .type(ActionType.TABLE_ACCESS)
                                .table(List.of("HR", "SALARY"))
                                .build()
                ))
                .build();

        assertEquals("analysts", policy.getName());
        assertEquals(2, policy.getActions().size());
    }

    @Test
    void policySet_construction() {
        var policySet = PolicySet.of(
                Policy.builder().name("analysts").actions(List.of()).build(),
                Policy.builder().name("anonymous").actions(List.of()).build()
        );

        assertEquals(2, policySet.getPolicies().size());
    }

    @Test
    void policyActionEntry_noExpression() {
        var entry = PolicyActionEntry.builder()
                .verb(ActionVerb.ALLOW)
                .type(ActionType.TABLE_ACCESS)
                .table(List.of("S", "T"))
                .build();

        assertFalse(entry.hasExpression());
    }

    @Test
    void policyActionEntry_blankRawExpression_notHasExpression() {
        var entry = PolicyActionEntry.builder()
                .verb(ActionVerb.ALLOW)
                .type(ActionType.ROW_FILTER)
                .table(List.of("S", "T"))
                .rawExpression("   ")
                .build();

        assertFalse(entry.hasExpression());
    }
}
