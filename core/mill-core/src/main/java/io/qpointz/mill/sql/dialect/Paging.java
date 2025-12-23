package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * Configuration for result set paging in SQL dialects.
 * 
 * @param limit LIMIT clause pattern (e.g., "LIMIT {n}" or "FETCH FIRST {n} ROWS ONLY")
 * @param offset OFFSET clause pattern (e.g., "OFFSET {m}" or "LIMIT {m}, {n}")
 * @param top TOP clause pattern (e.g., "TOP {n}") or null if not supported
 */
public record Paging(
    @JsonProperty("limit") String limit,
    @JsonProperty("offset") String offset,
    @JsonProperty("top") Optional<String> top
) {}
