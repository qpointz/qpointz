package io.qpointz.mill.services.metadata.impl;

import io.qpointz.mill.services.metadata.AnnotationsRepository;
import io.qpointz.mill.services.metadata.MetadataProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Collection<MetadataProvider.ValueMappingWithContext> getAllValueMappings() {
        return List.of();
    }

    @Override
    public Collection<MetadataProvider.ValueMappingSourceWithContext> getAllValueMappingSources() {
        return List.of();
    }
}
