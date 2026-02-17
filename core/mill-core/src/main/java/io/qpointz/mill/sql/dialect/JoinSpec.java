package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * Configuration for a specific join type in SQL dialects.
 * 
 * @param enabled Whether this join type is enabled/supported
 * @param keyword The SQL keyword for this join type (e.g., "LEFT JOIN", "LEFT OUTER JOIN")
 * @param requireOn Whether an ON clause is required
 * @param nullSafe Whether null-safe joins are supported
 * @param notes Optional notes about this join type
 */
public record JoinSpec(
    @JsonProperty("enabled") Optional<Boolean> enabled,
    @JsonProperty("keyword") Optional<String> keyword,
    @JsonProperty("require-on") Optional<Boolean> requireOn,
    @JsonProperty("null-safe") Optional<Boolean> nullSafe,
    @JsonProperty("notes") Optional<Object> notes
) {}
