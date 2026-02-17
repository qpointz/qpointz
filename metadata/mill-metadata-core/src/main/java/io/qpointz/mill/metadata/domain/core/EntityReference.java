package io.qpointz.mill.metadata.domain.core;

import java.util.Objects;

/**
 * Reference to a metadata entity (used in cross-entity and unbound facets).
 */
public record EntityReference(
    String schema,
    String table,
    String attribute  // optional, can be null
) {
    /**
     * Get fully qualified name.
     *
     * @return FQN string
     */
    public String getFqn() {
        if (attribute != null && !attribute.isEmpty()) {
            return String.format("%s.%s.%s", schema, table, attribute);
        }
        return String.format("%s.%s", schema, table);
    }
    
    /**
     * Check if this reference matches an entity location.
     *
     * @param schema schema name
     * @param table table name
     * @param attribute attribute name (optional)
     * @return true if matches
     */
    public boolean matches(String schema, String table, String attribute) {
        return Objects.equals(this.schema, schema) &&
               Objects.equals(this.table, table) &&
               (attribute == null || this.attribute == null || Objects.equals(this.attribute, attribute));
    }
}

