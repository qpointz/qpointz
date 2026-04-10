package io.qpointz.mill.data.backend.calcite;

import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlDialect;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.calcite.avatica.util.Quoting.BACK_TICK;

/**
 * Bridges a v2 {@link SqlDialectSpec} to Apache Calcite parser conventions
 * ({@link Quoting}, {@link Casing}).
 *
 * <p>Use {@link #asProperties(Map)} or {@link #asMap(Map)} to obtain settings
 * suitable for passing to a Calcite connection or parser configuration.</p>
 *
 * @param dialectSpec the dialect specification to derive conventions from
 */
@Slf4j
public record CalciteSqlDialectConventions(SqlDialectSpec dialectSpec) {

    /**
     * Maps the dialect's quote character to the corresponding Calcite {@link Quoting} enum.
     *
     * @return the Calcite quoting convention
     * @throws IllegalArgumentException if the dialect uses an unsupported quote character
     */
    public Quoting quoting() {
        val pair = dialectSpec.getIdentifiers().getQuote();
        return switch (pair.getStart()) {
            case "\"" -> Quoting.DOUBLE_QUOTE;
            case "`" -> BACK_TICK;
            case "'" -> Quoting.SINGLE_QUOTE;
            case "[" -> Quoting.BRACKET;
            default -> throw new IllegalArgumentException("Unknown quoting: " + pair.getStart());
        };
    }

    /**
     * Maps v2 unquoted storage convention to Calcite {@link Casing}.
     *
     * @return the Calcite casing convention for unquoted identifiers
     */
    public Casing unquotedCasing() {
        val storage = dialectSpec.getIdentifiers().getUnquotedStorage();
        return switch (storage) {
            case "UPPER" -> Casing.TO_UPPER;
            case "LOWER" -> Casing.TO_LOWER;
            case "AS_IS" -> Casing.UNCHANGED;
            default -> throw new IllegalArgumentException("Unknown unquoted storage: " + storage);
        };
    }

    private Map<String, Object> defaultMap() {
        return Map.of(
             "quoting", quoting().toString(),
             "caseSensitive", "true",
             "unquotedCasing", unquotedCasing().toString()
        );
    }

    /**
     * Returns conventions as a mutable map, merged with caller-supplied overrides.
     * Overrides take precedence; default convention keys are added only when absent.
     *
     * @param overrides caller-supplied entries that take precedence over defaults
     * @return merged map of convention settings
     */
    public Map<String, Object> asMap(Map<String, Object> overrides) {
        val map = defaultMap();
        val result = new HashMap(overrides);
        map.keySet().forEach(key -> result.putIfAbsent(key, map.get(key)));
        return result;
    }

    /**
     * Returns conventions as a {@link Properties} instance ready for Calcite configuration.
     *
     * @return properties containing {@code quoting}, {@code caseSensitive}, and {@code unquotedCasing}
     */
    public Properties asProperties(Map<String, Object> overrides) {
        val props = new Properties();
        props.putAll(asMap(overrides));
        return props;
    }

    public SqlDialect sqlDialect() {
        val calciteDatabaseProduct = SqlDialect.DatabaseProduct.valueOf(this.dialectSpec.getId().toUpperCase());
        if (calciteDatabaseProduct == SqlDialect.DatabaseProduct.UNKNOWN) {
            log.error("Unknown or unsupported SQL dialect: {}", this.dialectSpec.getId());
            throw new IllegalArgumentException("Unknown or unsupported SQL dialect: " + this.dialectSpec.getId());
        }
        return calciteDatabaseProduct.getDialect();
    }

}
