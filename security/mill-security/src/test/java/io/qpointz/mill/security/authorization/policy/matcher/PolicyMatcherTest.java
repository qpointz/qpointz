package io.qpointz.mill.security.authorization.policy.matcher;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyMatcherTest {

    private final PolicyMatcher matcher = new PolicyMatcher();

    @Test
    void matchesTable_exactMatch() {
        assertTrue(matcher.matchesTable(List.of("SALES", "CLIENT"), List.of("SALES", "CLIENT")));
    }

    @Test
    void matchesTable_noMatch() {
        assertFalse(matcher.matchesTable(List.of("SALES", "CLIENT"), List.of("SALES", "ORDERS")));
    }

    @Test
    void matchesTable_trailingWildcard() {
        assertTrue(matcher.matchesTable(List.of("SALES", "*"), List.of("SALES", "CLIENT")));
        assertTrue(matcher.matchesTable(List.of("SALES", "*"), List.of("SALES", "ORDERS")));
    }

    @Test
    void matchesTable_leadingWildcard() {
        assertTrue(matcher.matchesTable(List.of("*", "CLIENT"), List.of("SALES", "CLIENT")));
        assertTrue(matcher.matchesTable(List.of("*", "CLIENT"), List.of("HR", "CLIENT")));
    }

    @Test
    void matchesTable_midSegmentWildcard() {
        assertTrue(matcher.matchesTable(List.of("SALES", "AUDIT_*"), List.of("SALES", "AUDIT_LOG")));
        assertFalse(matcher.matchesTable(List.of("SALES", "AUDIT_*"), List.of("SALES", "CLIENT")));
    }

    @Test
    void matchesTable_caseInsensitive() {
        assertTrue(matcher.matchesTable(List.of("sales", "*"), List.of("SALES", "CLIENT")));
        assertTrue(matcher.matchesTable(List.of("SALES", "client"), List.of("sales", "CLIENT")));
    }

    @Test
    void matchesTable_segmentCountMismatch() {
        assertFalse(matcher.matchesTable(List.of("SALES", "*"), List.of("SALES", "PUBLIC", "T")));
    }

    @Test
    void matchesTable_singleSegment() {
        assertTrue(matcher.matchesTable(List.of("orders"), List.of("orders")));
        assertFalse(matcher.matchesTable(List.of("orders"), List.of("clients")));
    }

    @Test
    void matchesTable_nullInputs() {
        assertFalse(matcher.matchesTable(null, List.of("SALES")));
        assertFalse(matcher.matchesTable(List.of("SALES"), null));
        assertFalse(matcher.matchesTable(null, null));
    }

    @Test
    void matchesColumn_wildcardPrefix() {
        assertTrue(matcher.matchesColumn("pii_*", "pii_ssn"));
        assertTrue(matcher.matchesColumn("pii_*", "pii_dob"));
        assertFalse(matcher.matchesColumn("pii_*", "name"));
    }

    @Test
    void matchesColumn_exact() {
        assertTrue(matcher.matchesColumn("ssn", "ssn"));
        assertFalse(matcher.matchesColumn("ssn", "name"));
    }

    @Test
    void matchesColumn_caseInsensitive() {
        assertTrue(matcher.matchesColumn("PII_*", "pii_dob"));
        assertTrue(matcher.matchesColumn("ssn", "SSN"));
    }

    @Test
    void matchesColumn_nullInputs() {
        assertFalse(matcher.matchesColumn(null, "col"));
        assertFalse(matcher.matchesColumn("col", null));
    }

    @Test
    void matchesTable_wildcardOnly() {
        assertTrue(matcher.matchesTable(List.of("*", "*"), List.of("ANY", "TABLE")));
    }

    @Test
    void matchesColumn_wildcardSuffix() {
        assertTrue(matcher.matchesColumn("*_id", "client_id"));
        assertTrue(matcher.matchesColumn("*_id", "order_id"));
        assertFalse(matcher.matchesColumn("*_id", "name"));
    }
}
