package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Configuration for literal handling in SQL dialects.
 * 
 * @param strings String literal configuration
 * @param booleans List of boolean literal values (e.g., ["TRUE", "FALSE"] or ["1", "0"])
 * @param null_ The NULL literal representation
 * @param datesTimes Date/time literal configurations
 */
public record Literals(
    @JsonProperty("strings") StringLiterals strings,
    @JsonProperty("booleans") List<String> booleans,
    @JsonProperty("null") String null_,
    @JsonProperty("dates-times") DatesTimes datesTimes
) {}
