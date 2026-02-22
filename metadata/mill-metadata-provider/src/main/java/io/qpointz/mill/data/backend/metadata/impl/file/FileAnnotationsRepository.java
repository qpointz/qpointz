package io.qpointz.mill.data.backend.metadata.impl.file;

import io.qpointz.mill.data.backend.metadata.AnnotationsRepository;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;

import java.util.Optional;

public class FileAnnotationsRepository implements AnnotationsRepository {

    private final FileRepository repository;

    public FileAnnotationsRepository(FileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> getModelName() {
        return repository.model().name();
    }

    @Override
    public Optional<String> getModelDescription() {
        return repository.model().description();
    }

    private Optional<FileRepository.Schema> schemaByName(String schemaName) {
        if (schemaName==null) {
            return Optional.empty();
        }
        return this.repository.schemas().stream()
                .filter(k -> k.name().compareToIgnoreCase(schemaName)==0)
                .findFirst();
    }

    @Override
    public Optional<String> getSchemaDescription(String schemaName) {
        return schemaByName(schemaName)
                .flatMap(k -> k.description());
    }

    @Override
    public Optional<String> getTableDescription(String schemaName, String tableName) {
        return tableByName(schemaName, tableName)
                .flatMap(FileRepository.Table::description);
    }

    private Optional<FileRepository.Table> tableByName(String schemaName, String tableName) {
        return schemaByName(schemaName).flatMap(k-> k.tables().stream()
                .filter(z-> z.name().compareToIgnoreCase(tableName)==0)
                .findFirst()
        );
    }

    @Override
    public Optional<String> getAttributeDescription(String schemaName, String tableName, String attributeNam) {
        return attributeByName(schemaName, tableName, attributeNam)
                .flatMap(FileRepository.Attribute::description);
    }

    private Optional<FileRepository.Attribute> attributeByName(String schemaName, String tableName, String attributeNam) {
        return tableByName(schemaName,tableName).flatMap(k-> k.attributes().stream()
                .filter(z-> z.name().compareToIgnoreCase(attributeNam)==0)
                .findFirst()
        );
    }

    @Override
    public java.util.Collection<MetadataProvider.ValueMappingWithContext> getAllValueMappings() {
        return repository.schemas().stream()
            .flatMap(schema -> schema.tables().stream()
                .flatMap(table -> table.attributes().stream()
                    .filter(attr -> attr.valueMappings().isPresent())
                    .flatMap(attr -> {
                        var valueMappings = attr.valueMappings().get();
                        var mappings = valueMappings.mappings();
                        return mappings.stream()
                            .map(mapping -> new MetadataProvider.ValueMappingWithContext(
                                schema.name(),
                                table.name(),
                                attr.name(),
                                valueMappings.context(),
                                valueMappings.similarityThreshold(),
                                mapping
                            ))
                            .flatMap(MetadataProvider.ValueMappingWithContext::expand);
                    })
                )
            )
            .toList();
    }

    @Override
    public java.util.Collection<MetadataProvider.ValueMappingSourceWithContext> getAllValueMappingSources() {
        return repository.schemas().stream()
            .flatMap(schema -> schema.tables().stream()
                .flatMap(table -> table.attributes().stream()
                    .filter(attr -> attr.valueMappings().isPresent())
                    .flatMap(attr -> {
                        var valueMappings = attr.valueMappings().get();
                        var sources = valueMappings.sources();
                        return sources.stream()
                            .filter(FileRepository.ValueMappingSource::isEnabled)
                            .map(source -> new MetadataProvider.ValueMappingSourceWithContext(
                                schema.name(),
                                table.name(),
                                attr.name(),
                                valueMappings.context(),
                                valueMappings.similarityThreshold(),
                                source.name(),
                                source.sql(),
                                source.description().orElse(""),
                                source.isEnabled(),
                                source.getCacheTtl()
                            ));
                    })
                )
            )
            .toList();
    }


}
