package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * Configuration for ORDER BY clause in SQL dialects.
 * 
 * @param orderByNulls NULLS FIRST/LAST support (e.g., "NULLS FIRST/LAST", "NULLS LAST", or null if unsupported)
 * @param notes Optional notes about ordering behavior (can be String or List<String> in YAML)
 */
public record Ordering(
    @JsonProperty("order-by-nulls") Optional<String> orderByNulls,
    @JsonProperty("notes") Optional<Object> notes
) {}