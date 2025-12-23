package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for ON clause handling in SQL joins.
 * 
 * @param keyword The keyword used for ON clause (typically "ON")
 * @param requireCondition Whether a condition is required in the ON clause
 */
public record OnClause(
    @JsonProperty("keyword") String keyword,
    @JsonProperty("require-condition") Boolean requireCondition
) {}
