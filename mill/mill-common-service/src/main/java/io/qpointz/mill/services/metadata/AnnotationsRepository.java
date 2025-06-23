package io.qpointz.mill.services.metadata;

import java.util.Optional;

public interface AnnotationsRepository {
    Optional<String> getModelName();

    Optional<String> getModelDescription();

    Optional<String> getSchemaDescription(String schemaName);

    Optional<String> getTableDescription(String schemaName, String tableName);

    Optional<String> getAttributeDescription(String schemaName, String tableName, String attributeNam);
}
