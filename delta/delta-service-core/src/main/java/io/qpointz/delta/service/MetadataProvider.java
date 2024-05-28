package io.qpointz.delta.service;

import io.qpointz.delta.proto.Schema;

import java.util.Optional;

public interface MetadataProvider {
    Iterable<String> getSchemaNames();
    Optional<Schema> getSchema(String schemaName);
}
