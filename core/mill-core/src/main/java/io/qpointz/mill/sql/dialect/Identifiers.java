package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * Configuration for identifier handling in SQL dialects.
 * 
 * @param case_ The case handling for identifiers
 * @param quote Quote characters for identifiers
 * @param aliasQuote Quote characters for aliases
 * @param useFullyQualifiedNames Whether to use fully qualified names (schema.table.column)
 * @param notes Optional notes about identifier handling
 */
public record Identifiers(
    @JsonProperty("case") IdentifierCase case_,
    @JsonProperty("quote") QuotePair quote,
    @JsonProperty("alias-quote") QuotePair aliasQuote,
    @JsonProperty("use-fully-qualified-names") Boolean useFullyQualifiedNames,
    @JsonProperty("notes") Optional<List<String>> notes
) {}
