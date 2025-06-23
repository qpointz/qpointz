package io.qpointz.mill.services.metadata.model;

import java.util.Optional;

public record Attribute(String schemaName,
                        String tableName,
                        String name,
                        String typeName,
                        boolean nullable,
                        Optional<String> description) {
}
