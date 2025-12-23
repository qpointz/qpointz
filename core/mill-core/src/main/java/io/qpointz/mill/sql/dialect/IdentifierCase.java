package io.qpointz.mill.sql.dialect;

/**
 * Enumeration for identifier case handling in SQL dialects.
 */
public enum IdentifierCase {
    /**
     * Identifiers are converted to uppercase.
     */
    UPPER,
    
    /**
     * Identifiers are converted to lowercase.
     */
    LOWER,
    
    /**
     * Identifiers are kept as-is (case-sensitive).
     */
    AS_IS
}
