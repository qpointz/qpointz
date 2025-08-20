package io.qpointz.mill.ai.nlsql.models;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
abstract class SpecSqlDialectTestBase {

    protected abstract SqlDialect dialect();

    protected SpecSqlDialect spec() {
        return (SpecSqlDialect) dialect();
    }

    @Test
    void trivia() {
        assertDoesNotThrow(this::dialect);
        assertNotNull(dialect());
    }

    @Test
    void renderDoesntThrow() {
        assertDoesNotThrow(() -> dialect()
                .getConventionsSpec(SqlDialect.SqlFeatures.DEFAULT)
                .getText());
    }

    @Test
    void renderDefaultPrompt() {
        log.info(dialect()
                .getConventionsSpec(SqlDialect.SqlFeatures.DEFAULT)
                .getText());
    }

    @Test
    void returnsId() {
        val id = dialect().getId();
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    void hasAllSections() {
        val map = spec().getSqlDialectSpec();
        val keys = map.keySet();
        val expects = Set.of("id", "name",
                "identifiers", "literals",
                "joins", "ordering", "paging", "operators", "functions", "notes");
        assertEquals(expects, keys);
    }

    void subSectionTest(String groupKey, Set<String> expectKeys) {
        val groupKeys = ((Map<String,Object>)(spec()
                .getSqlDialectSpec()
                .get(groupKey))).keySet();
        assertEquals(expectKeys, groupKeys);
    }

    @Test
    void identifiersTest() {
        subSectionTest("identifiers",
                Set.of("case", "quote", "alias-quote", "use-fully-qualified-names"));
    }

    @Test
    void literalsTest() {
        subSectionTest("literals",
                Set.of("strings", "booleans", "null", "dates-times"));
    }

    @Test
    void operatorsTest() {
        subSectionTest("operators",
                Set.of("equality", "inequality",
                       "comparison", "arithmetic_operators", "null_checks",
                       "set", "null-safe", "logical", "like",
                        "between", "casting", "regex"));
    }

    @Test
    void functionsTest() {
        subSectionTest("functions",
                Set.of("strings", "regex", "numerics",
                        "aggregates", "dates_times", "conditionals"));
    }

}