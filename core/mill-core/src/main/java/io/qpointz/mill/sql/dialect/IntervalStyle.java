package io.qpointz.mill.sql.dialect;

/**
 * Enumeration for INTERVAL literal style in SQL dialects.
 */
public enum IntervalStyle {
    /**
     * ANSI SQL INTERVAL syntax (e.g., INTERVAL '1' DAY).
     */
    ANSI,
    
    /**
     * Keyword-based syntax (varies by dialect).
     */
    KEYWORD,
    
    /**
     * Function-based syntax (e.g., DATE_ADD with INTERVAL keyword).
     */
    FUNCTION,
    
    /**
     * MySQL-style INTERVAL usage.
     */
    MYSQL
}
