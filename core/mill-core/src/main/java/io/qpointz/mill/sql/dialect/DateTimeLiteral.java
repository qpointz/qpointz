package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * Configuration for date/time literal syntax in SQL dialects.
 * 
 * @param syntax The syntax pattern (e.g., "DATE 'YYYY-MM-DD'")
 * @param quote The quote character used
 * @param pattern The date/time pattern (e.g., "YYYY-MM-DD")
 * @param notes Optional notes about the literal syntax
 */
public record DateTimeLiteral(
    @JsonProperty("syntax") String syntax,
    @JsonProperty("quote") String quote,
    @JsonProperty("pattern") String pattern,
    @JsonProperty("notes") Optional<List<String>> notes
) {}
