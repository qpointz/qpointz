package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * Represents a single operator entry in SQL dialect configuration.
 * 
 * @param symbol The operator symbol (e.g., "=", ">=", "LIKE", "CAST")
 * @param syntax Optional syntax pattern (e.g., "CAST({expr} AS {type})")
 * @param description Optional description of the operator
 * @param supported Whether the operator is supported (used primarily for casting operators)
 */
public record OperatorEntry(
    @JsonProperty("symbol") String symbol,
    @JsonProperty("syntax") Optional<String> syntax,
    @JsonProperty("description") Optional<String> description,
    @JsonProperty("supported") Optional<Boolean> supported
) {}
