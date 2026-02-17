package io.qpointz.mill.services.metadata.impl.v2;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet;
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet;
import io.qpointz.mill.metadata.service.MetadataService;
import io.qpointz.mill.services.metadata.AnnotationsRepository;
import io.qpointz.mill.services.metadata.MetadataProvider;
import io.qpointz.mill.services.metadata.impl.file.FileRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class MetadataV2AnniotationsProvider implements AnnotationsRepository {

    private final MetadataService metadataService;

    public MetadataV2AnniotationsProvider(MetadataService repository) {
        log.info("Using metadata v2 annotations provider");
        this.metadataService = repository;
    }

    @Override
    public Optional<String> getModelName() {
        // Model concept not present in v2 format
        return Optional.empty();
    }

    @Override
    public Optional<String> getModelDescription() {
        // Model concept not present in v2 format
        return Optional.empty();
    }

    @Override
    public Optional<String> getSchemaDescription(String schemaName) {
        return metadataService.findByLocation(schemaName, null, null)
                .flatMap(entity -> entity.getFacet("descriptive", "global", DescriptiveFacet.class))
                .map(DescriptiveFacet::getDescription);
    }

    @Override
    public Optional<String> getTableDescription(String schemaName, String tableName) {
        return metadataService.findByLocation(schemaName, tableName, null)
                .flatMap(entity -> entity.getFacet("descriptive", "global", DescriptiveFacet.class))
                .map(DescriptiveFacet::getDescription);
    }

    @Override
    public Optional<String> getAttributeDescription(String schemaName, String tableName, String attributeName) {
        return metadataService.findByLocation(schemaName, tableName, attributeName)
                .flatMap(entity -> entity.getFacet("descriptive", "global", DescriptiveFacet.class))
                .map(DescriptiveFacet::getDescription);
    }

    @Override
    public Collection<MetadataProvider.ValueMappingWithContext> getAllValueMappings() {
        List<MetadataProvider.ValueMappingWithContext> result = new ArrayList<>();
        
        List<MetadataEntity> attributes = metadataService.findByType(MetadataType.ATTRIBUTE);
        for (MetadataEntity attribute : attributes) {
            Optional<ValueMappingFacet> valueMappingFacetOpt = attribute.getFacet("value-mapping", "global", ValueMappingFacet.class);
            if (valueMappingFacetOpt.isPresent()) {
                ValueMappingFacet valueMappingFacet = valueMappingFacetOpt.get();
                
                String context = valueMappingFacet.getContext();
                Double similarityThreshold = valueMappingFacet.getSimilarityThreshold();
                
                if (valueMappingFacet.getMappings() != null) {
                    for (ValueMappingFacet.ValueMapping mappingData : valueMappingFacet.getMappings()) {
                        FileRepository.ValueMapping mapping = createValueMapping(mappingData);
                        MetadataProvider.ValueMappingWithContext contextMapping = new MetadataProvider.ValueMappingWithContext(
                                attribute.getSchemaName(),
                                attribute.getTableName(),
                                attribute.getAttributeName(),
                                Optional.ofNullable(context),
                                Optional.ofNullable(similarityThreshold),
                                mapping
                        );
                        result.add(contextMapping);
                    }
                }
            }
        }
        
        // Expand aliases
        return result.stream()
                .flatMap(MetadataProvider.ValueMappingWithContext::expand)
                .toList();
    }

    @Override
    public Collection<MetadataProvider.ValueMappingSourceWithContext> getAllValueMappingSources() {
        List<MetadataProvider.ValueMappingSourceWithContext> result = new ArrayList<>();
        
        List<MetadataEntity> attributes = metadataService.findByType(MetadataType.ATTRIBUTE);
        for (MetadataEntity attribute : attributes) {
            Optional<ValueMappingFacet> valueMappingFacetOpt = attribute.getFacet("value-mapping", "global", ValueMappingFacet.class);
            if (valueMappingFacetOpt.isPresent()) {
                ValueMappingFacet valueMappingFacet = valueMappingFacetOpt.get();
                
                String context = valueMappingFacet.getContext();
                Double similarityThreshold = valueMappingFacet.getSimilarityThreshold();
                
                if (valueMappingFacet.getSources() != null) {
                    for (ValueMappingFacet.ValueMappingSource sourceData : valueMappingFacet.getSources()) {
                        if (!sourceData.isEnabled()) {
                            continue; // Skip disabled sources
                        }
                        
                        String sourceName = sourceData.name();
                        String sql = sourceData.getSql();
                        String description = sourceData.description();
                        int cacheTtl = sourceData.getCacheTtl();
                        
                        MetadataProvider.ValueMappingSourceWithContext sourceContext = new MetadataProvider.ValueMappingSourceWithContext(
                                attribute.getSchemaName(),
                                attribute.getTableName(),
                                attribute.getAttributeName(),
                                Optional.ofNullable(context),
                                Optional.ofNullable(similarityThreshold),
                                sourceName != null ? sourceName : "unknown",
                                sql != null ? sql : "",
                                description != null ? description : "",
                                true,
                                cacheTtl
                        );
                        result.add(sourceContext);
                    }
                }
            }
        }
        
        return result;
    }

    private FileRepository.ValueMapping createValueMapping(ValueMappingFacet.ValueMapping mappingData) {
        return new FileRepository.ValueMapping(
                mappingData.userTerm(),
                mappingData.databaseValue(),
                Optional.ofNullable(mappingData.displayValue()),
                Optional.ofNullable(mappingData.description()),
                Optional.of(mappingData.language()),
                Optional.ofNullable(mappingData.aliases())
        );
    }
}
