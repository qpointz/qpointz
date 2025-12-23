package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a pair of quote characters used for identifiers or aliases.
 * 
 * @param start The starting quote character
 * @param end The ending quote character
 */
public record QuotePair(
    @JsonProperty("start") String start,
    @JsonProperty("end") String end
) {}
