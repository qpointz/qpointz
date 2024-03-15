package io.qpointz.delta.service;

import io.qpointz.delta.proto.Schema;
import io.qpointz.delta.proto.Table;

import java.util.Optional;

public interface SchemaProvider {
    Iterable<String> getSchemaNames();
    Optional<Schema> getSchema(String schemaName);
}
