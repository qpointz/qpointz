package io.qpointz.mill.metadata.domain.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Target reference for a concept - represents a table with optional attributes.
 * Used in ConceptFacet to reference which tables and attributes a concept relates to.
 */
public record ConceptTarget(
    String schema,
    String table,
    List<String> attributes  // optional, defaults to empty list
) {
    /**
     * Constructor with default empty list for attributes.
     */
    public ConceptTarget {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }
    }
    
    /**
     * Get fully qualified table name.
     *
     * @return FQN string (schema.table)
     */
    public String getFqn() {
        return String.format("%s.%s", schema, table);
    }
}

