package io.qpointz.mill.data.backend;

import io.qpointz.mill.proto.Schema;

public interface SchemaProvider {
    Iterable<String> getSchemaNames();
    Schema getSchema(String schemaName);
    boolean isSchemaExists(String schemaName);
}
