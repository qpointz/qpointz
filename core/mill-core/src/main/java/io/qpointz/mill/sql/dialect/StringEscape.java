package io.qpointz.mill.sql.dialect;

/**
 * Enumeration for string escape handling in SQL dialects.
 */
public enum StringEscape {
    /**
     * Standard SQL escape (single quote doubled: '').
     */
    STANDARD,
    
    /**
     * Backslash escape (e.g., \').
     */
    BACKSLASH,
    
    /**
     * Doubled quote escape (e.g., '' for single quote).
     */
    DOUBLED
}
