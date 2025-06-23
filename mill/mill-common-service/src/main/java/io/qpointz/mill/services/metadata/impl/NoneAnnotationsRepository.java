package io.qpointz.mill.services.metadata.impl;

import io.qpointz.mill.services.metadata.AnnotationsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Lazy
@Component
@ConditionalOnProperty(prefix = "mill.metadata", name = "annotations", havingValue = "none")
public class NoneAnnotationsRepository implements AnnotationsRepository {
    @Override
    public Optional<String> getModelName() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getModelDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSchemaDescription(String schemaName) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getTableDescription(String schemaName, String tableName) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getAttributeDescription(String schemaName, String tableName, String attributeNam) {
        return Optional.empty();
    }
}

