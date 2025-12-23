package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * Configuration for INTERVAL literal syntax in SQL dialects.
 * 
 * @param supported Whether INTERVAL literals are supported
 * @param style The style of INTERVAL syntax
 * @param notes Optional notes about INTERVAL literal handling
 */
public record IntervalLiteral(
    @JsonProperty("supported") Boolean supported,
    @JsonProperty("style") IntervalStyle style,
    @JsonProperty("notes") Optional<List<String>> notes
) {}
