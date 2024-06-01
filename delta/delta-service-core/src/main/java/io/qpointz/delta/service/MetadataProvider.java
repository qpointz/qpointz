package io.qpointz.delta.service;

import io.qpointz.delta.proto.Schema;

public interface MetadataProvider {
    Iterable<String> getSchemaNames();
    Schema getSchema(String schemaName);
    boolean isSchemaExists(String schemaName);
}
