package io.qpointz.mill.metadata.domain.core;

/**
 * Type of database table object.
 * Moved to core package to avoid conflicts.
 */
public enum TableType {
    TABLE,
    VIEW,
    MATERIALIZED_VIEW,
    EXTERNAL
}

