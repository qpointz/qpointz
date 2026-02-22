package io.qpointz.mill.data.backend.metadata.impl;

import io.qpointz.mill.data.backend.metadata.model.*;
import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.metadata.AnnotationsRepository;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;
import io.qpointz.mill.data.backend.metadata.RelationsProvider;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class MetadataProviderImpl implements MetadataProvider {

    public static final String METADATA_SCHEMA = "metadata";
    private final SchemaProvider schemaProvider;
    private final AnnotationsRepository annotationsRepository;
    private final RelationsProvider relationsProvider;

    public MetadataProviderImpl(SchemaProvider schemaProvider,
                                AnnotationsRepository annotationsRepository,
                                RelationsProvider relationsProvider) {
        this.schemaProvider = schemaProvider;
        this.annotationsRepository = annotationsRepository;
        this.relationsProvider = relationsProvider;
    }

    @Override
    public Model getModel() {
        val description = this.annotationsRepository.getModelDescription();
        val name = this.annotationsRepository.getModelName();
        return new Model(name, description);
    }

    private List<String> getSchemaNames() {
        return StreamSupport.stream(this.schemaProvider.getSchemaNames().spliterator(),false)
                .filter(k -> k.compareToIgnoreCase(METADATA_SCHEMA)!=0) //exclude calcite metadata schema from metadata
                .toList();
    }

    @Override
    public Collection<Schema> getSchemas() {
        return getSchemaNames().stream()
                .map(k-> new Schema(k,
                        this.annotationsRepository.getSchemaDescription(k)))
                .toList();

    }

    @Override
    public Collection<Table> getTables(String schemaName) {
        if (schemaName.compareToIgnoreCase(METADATA_SCHEMA)==0) {
            return List.of();
        }
        val tables = this.schemaProvider.getSchema(schemaName)
                .getTablesList();

        return tables.stream()
                .map(k-> {
                    val phSchemaName = k.getSchemaName();
                    val phName = k.getName();
                    return new Table(phSchemaName, phName,
                            getTableAttributes(k) ,
                            this.annotationsRepository.getTableDescription(phSchemaName, phName));

                })
                .toList();
    }

    private Collection<Attribute> getTableAttributes(io.qpointz.mill.proto.Table table) {
        val schemaName = table.getSchemaName();
        val name = table.getName();
        return table.getFieldsList().stream()
                .map(k-> new Attribute(schemaName, name, k.getName(),
                                        k.getType().getType().getTypeId().name(),
                                k.getType().getNullability() == DataType.Nullability.NULL,
                                        this.annotationsRepository.getAttributeDescription(schemaName, name, k.getName())
                                ))
                .toList();
    }

    @Override
    public Collection<Relation> getRelations() {
        return this.relationsProvider.getRelations();
    }

    @Override
    public Optional<Table> getTable(String schemaName, String tableName) {
        val mayBeSchema = this.getSchemaNames().stream()
                .filter(k-> k.compareToIgnoreCase(schemaName)==0)
                .findFirst();
        return mayBeSchema.flatMap(s -> this.getTables(s).stream()
                .filter(k -> k.name().compareToIgnoreCase(tableName) == 0)
                .findFirst());

    }

    @Override
    public Collection<ValueMappingWithContext> getAllValueMappings() {
        return this.annotationsRepository.getAllValueMappings();
    }

    @Override
    public Collection<ValueMappingSourceWithContext> getAllValueMappingSources() {
        return this.annotationsRepository.getAllValueMappingSources();
    }
}
