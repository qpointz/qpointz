package io.qpointz.mill.data.backend;

import io.qpointz.mill.proto.Schema;
import io.qpointz.mill.proto.Table;

/**
 * Supplies physical catalog snapshots for schema exploration and logical layout projection.
 */
public interface SchemaProvider {
    Iterable<String> getSchemaNames();

    Schema getSchema(String schemaName);

    boolean isSchemaExists(String schemaName);

    /**
     * Resolves a single physical table in {@code schemaName} without forcing callers to enumerate
     * unrelated tables. Implementations that can resolve one Calcite/JDBC table cheaply should override
     * this instead of relying on the default.
     *
     * @param schemaName physical schema name; must not be null
     * @param tableName physical table name; must not be null
     * @return Mill protobuf table (fields populated), or {@code null} if the schema or table is missing
     */
    default Table getTable(String schemaName, String tableName) {
        if (tableName == null || !isSchemaExists(schemaName)) {
            return null;
        }
        Schema schema = getSchema(schemaName);
        for (Table t : schema.getTablesList()) {
            if (tableName.equals(t.getName())) {
                return t;
            }
        }
        return null;
    }
}
