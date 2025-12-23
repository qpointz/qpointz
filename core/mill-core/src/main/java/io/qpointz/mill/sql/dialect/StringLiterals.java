package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * Configuration for string literal handling in SQL dialects.
 * 
 * @param quote Quote character for strings (typically single quote)
 * @param concat Concatenation operator (e.g., "||" or "CONCAT")
 * @param escape Escape style for special characters
 * @param note Optional note about string literal handling
 */
public record StringLiterals(
    @JsonProperty("quote") String quote,
    @JsonProperty("concat") String concat,
    @JsonProperty("escape") StringEscape escape,
    @JsonProperty("note") Optional<String> note
) {}
