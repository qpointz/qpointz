package io.qpointz.mill.metadata.model;

import java.util.List;
import java.util.Optional;

/**
 * Value mapping for an attribute.
 * Maps user terms (e.g., "premium", "California") to database values (e.g., "PREMIUM", "CA").
 */
public record ValueMapping(
    String userTerm,
    String databaseValue,
    String displayValue,
    String language,
    Optional<String> description,
    Optional<List<String>> aliases
) {
    
    /**
     * Create a value mapping with minimal fields
     */
    public ValueMapping(String userTerm, String databaseValue) {
        this(userTerm, databaseValue, databaseValue, "en", Optional.empty(), Optional.empty());
    }
    
    /**
     * Create a value mapping with language
     */
    public ValueMapping(String userTerm, String databaseValue, String language) {
        this(userTerm, databaseValue, databaseValue, language, Optional.empty(), Optional.empty());
    }
}

