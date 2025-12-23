package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * Represents a single function entry in SQL dialect configuration.
 * 
 * @param name Function name (e.g., "SUBSTRING", "COUNT", "DATE_TRUNC")
 * @param synonyms Optional list of function synonyms (e.g., ["SUBSTR"] for SUBSTRING)
 * @param return_ Return type specification
 * @param syntax Function syntax pattern (e.g., "SUBSTRING(text FROM start FOR length)")
 * @param args Optional list of function arguments
 * @param notes Optional notes about the function
 */
public record FunctionEntry(
    @JsonProperty("name") String name,
    @JsonProperty("synonyms") Optional<List<String>> synonyms,
    @JsonProperty("return") ReturnType return_,
    @JsonProperty("syntax") String syntax,
    @JsonProperty("args") Optional<List<FunctionArg>> args,
    @JsonProperty("notes") Optional<List<String>> notes
) {}
