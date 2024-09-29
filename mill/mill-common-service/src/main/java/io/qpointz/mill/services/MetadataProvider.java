package io.qpointz.mill.services;

import io.qpointz.mill.proto.Schema;

public interface MetadataProvider {
    Iterable<String> getSchemaNames();
    Schema getSchema(String schemaName);
    boolean isSchemaExists(String schemaName);
}
