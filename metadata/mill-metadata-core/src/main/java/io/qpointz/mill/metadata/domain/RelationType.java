package io.qpointz.mill.metadata.domain;

/**
 * Type of relationship.
 */
public enum RelationType {
    /**
     * Physical foreign key constraint.
     */
    FOREIGN_KEY,
    
    /**
     * Business relationship (no FK constraint).
     */
    LOGICAL,
    
    /**
     * Parent-child hierarchy.
     */
    HIERARCHICAL
}

