package io.qpointz.mill.data.backend.calcite;

import io.qpointz.mill.sql.dialect.IdentifierCase;
import io.qpointz.mill.sql.dialect.Identifiers;
import io.qpointz.mill.sql.dialect.QuotePair;
import io.qpointz.mill.sql.dialect.SqlDialectSpec;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlDialect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CalciteSqlDialectConventions}.
 *
 * <p>All tests use a mocked {@link SqlDialectSpec} with only {@code identifiers()} stubbed,
 * keeping them isolated from dialect YAML resources. The {@link #conventionsFor} helper
 * builds the mock from a quote character and an {@link IdentifierCase}.</p>
 *
 * <p>When adding a new quoting or casing branch to the production code, add a corresponding
 * entry to {@link #quotingCases()} or {@link #casingCases()}.</p>
 */
class CalciteSqlDialectConventionsTest {

    private static CalciteSqlDialectConventions conventionsFor(String quoteStart, IdentifierCase identifierCase) {
        return conventionsFor(quoteStart, identifierCase, "CALCITE");
    }

    private static CalciteSqlDialectConventions conventionsFor(String quoteStart, IdentifierCase identifierCase, String dialectId) {
        var quote = new QuotePair(quoteStart, quoteStart);
        var identifiers = new Identifiers(identifierCase, quote, quote, false, Optional.empty());
        var spec = mock(SqlDialectSpec.class);
        when(spec.identifiers()).thenReturn(identifiers);
        when(spec.id()).thenReturn(dialectId);
        return new CalciteSqlDialectConventions(spec);
    }

    // -- quoting() --

    static Stream<Arguments> quotingCases() {
        return Stream.of(
                Arguments.of("\"", Quoting.DOUBLE_QUOTE),
                Arguments.of("`", Quoting.BACK_TICK),
                Arguments.of("'", Quoting.SINGLE_QUOTE),
                Arguments.of("[", Quoting.BRACKET)
        );
    }

    @ParameterizedTest
    @MethodSource("quotingCases")
    void shouldMapQuoteCharToQuoting(String quoteChar, Quoting expected) {
        assertThat(conventionsFor(quoteChar, IdentifierCase.UPPER).quoting())
                .isEqualTo(expected);
    }

    @Test
    void shouldThrowOnUnknownQuoting() {
        var conventions = conventionsFor("#", IdentifierCase.UPPER);
        assertThatThrownBy(conventions::quoting)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("#");
    }

    // -- unquotedCasing() --

    static Stream<Arguments> casingCases() {
        return Stream.of(
                Arguments.of(IdentifierCase.UPPER, Casing.TO_UPPER),
                Arguments.of(IdentifierCase.LOWER, Casing.TO_LOWER),
                Arguments.of(IdentifierCase.AS_IS, Casing.UNCHANGED)
        );
    }

    @ParameterizedTest
    @MethodSource("casingCases")
    void shouldMapIdentifierCaseToCasing(IdentifierCase identifierCase, Casing expected) {
        assertThat(conventionsFor("\"", identifierCase).unquotedCasing())
                .isEqualTo(expected);
    }

    // -- asProperties() --

    @Test
    void shouldReturnPropertiesWithExpectedKeys() {
        var props = conventionsFor("\"", IdentifierCase.UPPER).asProperties(Map.of());
        assertThat(props).containsKeys("quoting", "caseSensitive", "unquotedCasing");
        assertThat(props.getProperty("caseSensitive")).isEqualTo("true");
    }

    @Test
    void shouldReturnConsistentValuesInProperties() {
        var conventions = conventionsFor("`", IdentifierCase.LOWER);
        var props = conventions.asProperties(Map.of());
        assertThat(props.getProperty("quoting")).isEqualTo(conventions.quoting().toString());
        assertThat(props.getProperty("unquotedCasing")).isEqualTo(conventions.unquotedCasing().toString());
    }

    @Test
    void shouldMergeOverridesIntoProperties() {
        var overrides = Map.<String, Object>of("caseSensitive", "false");
        var props = conventionsFor("\"", IdentifierCase.UPPER).asProperties(overrides);
        assertThat(props.getProperty("caseSensitive")).isEqualTo("false");
        assertThat(props).containsKeys("quoting", "unquotedCasing");
    }

    // -- asMap(overrides) --

    @Test
    void shouldPreserveOverrides() {
        var overrides = Map.<String, Object>of("quoting", "CUSTOM_VALUE");
        var result = conventionsFor("\"", IdentifierCase.UPPER).asMap(overrides);
        assertThat(result.get("quoting")).isEqualTo("CUSTOM_VALUE");
    }

    @Test
    void shouldFillMissingKeysFromDefaults() {
        var overrides = Map.<String, Object>of("extraKey", "extraValue");
        var result = conventionsFor("\"", IdentifierCase.UPPER).asMap(overrides);
        assertThat(result).containsKeys("quoting", "caseSensitive", "unquotedCasing", "extraKey");
        assertThat(result.get("extraKey")).isEqualTo("extraValue");
    }

    @Test
    void shouldNotOverrideExistingKeys() {
        var overrides = Map.<String, Object>of(
                "quoting", "OVERRIDDEN",
                "caseSensitive", "false"
        );
        var result = conventionsFor("\"", IdentifierCase.UPPER).asMap(overrides);
        assertThat(result.get("quoting")).isEqualTo("OVERRIDDEN");
        assertThat(result.get("caseSensitive")).isEqualTo("false");
    }

    // -- sqlDialect() --

    @Test
    void shouldReturnCalciteDialect() {
        var conventions = conventionsFor("`", IdentifierCase.UPPER, "CALCITE");
        var dialect = conventions.sqlDialect();
        assertThat(dialect).isNotNull();
        assertThat(dialect).isEqualTo(SqlDialect.DatabaseProduct.CALCITE.getDialect());
    }

    @Test
    void shouldReturnH2Dialect() {
        var conventions = conventionsFor("\"", IdentifierCase.UPPER, "H2");
        var dialect = conventions.sqlDialect();
        assertThat(dialect).isNotNull();
        assertThat(dialect).isEqualTo(SqlDialect.DatabaseProduct.H2.getDialect());
    }

    @Test
    void shouldHandleLowercaseDialectId() {
        var conventions = conventionsFor("`", IdentifierCase.UPPER, "calcite");
        var dialect = conventions.sqlDialect();
        assertThat(dialect).isEqualTo(SqlDialect.DatabaseProduct.CALCITE.getDialect());
    }

    @Test
    void shouldThrowOnUnknownDialectId() {
        var conventions = conventionsFor("\"", IdentifierCase.UPPER, "NONEXISTENT");
        assertThatThrownBy(conventions::sqlDialect)
                .isInstanceOf(IllegalArgumentException.class);
    }

}
