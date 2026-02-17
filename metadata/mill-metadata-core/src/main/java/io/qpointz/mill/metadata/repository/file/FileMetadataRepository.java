package io.qpointz.mill.metadata.repository.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.repository.MetadataRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * File-based metadata repository using YAML format.
 * Reads entities from YAML file with document-style structure.
 */
@Slf4j
public class FileMetadataRepository implements MetadataRepository {
    
    private final Map<String, MetadataEntity> entities = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final ObjectMapper yamlMapper;
    private final ResourceResolver resourceResolver;
    private final List<String> locations;
    
    /**
     * Constructor accepting a single location (for backward compatibility).
     */
    public FileMetadataRepository(String location, ResourceResolver resourceResolver) {
        this(Arrays.asList(location), resourceResolver);
    }
    
    /**
     * Constructor accepting multiple file locations/patterns.
     * Files are loaded in order, with later files overriding earlier ones.
     *
     * @param locations list of file paths or patterns (e.g., "classpath:metadata/*.yml")
     * @param resourceResolver resolver for converting location patterns to input streams
     */
    public FileMetadataRepository(List<String> locations, ResourceResolver resourceResolver) {
        this.locations = new ArrayList<>(locations);
        this.resourceResolver = resourceResolver;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
        loadEntities();
    }
    
    private void loadEntities() {
        List<ResourceResolver.ResolvedResource> resources = resolveFilePatterns(locations);
        
        if (resources.isEmpty()) {
            log.warn("No metadata files found for locations: {}", locations);
            return;
        }
        
        int totalEntitiesLoaded = 0;
        for (ResourceResolver.ResolvedResource resource : resources) {
            try {
                int entitiesFromFile = loadEntitiesFromResource(resource);
                totalEntitiesLoaded += entitiesFromFile;
                log.debug("Loaded {} entities from {}", entitiesFromFile, resource.name());
            } catch (IOException e) {
                log.error("Failed to load metadata from file: {}", resource.name(), e);
                throw new RuntimeException("Failed to load metadata from file: " + resource.name(), e);
            }
        }
        
        log.info("Loaded {} total metadata entities from {} files", totalEntitiesLoaded, resources.size());
    }
    
    /**
     * Resolve file patterns to actual resources.
     * Supports glob patterns via the configured ResourceResolver.
     *
     * @param patterns list of file paths or patterns
     * @return ordered list of resources to load
     */
    private List<ResourceResolver.ResolvedResource> resolveFilePatterns(List<String> patterns) {
        List<ResourceResolver.ResolvedResource> resources = new ArrayList<>();
        
        for (String pattern : patterns) {
            try {
                List<ResourceResolver.ResolvedResource> resolved = resourceResolver.resolve(pattern);
                resources.addAll(resolved);
            } catch (IOException e) {
                log.warn("Failed to resolve file pattern: {}", pattern, e);
            }
        }
        
        return resources;
    }
    
    /**
     * Load entities from a single resource.
     * Merges entities with existing ones: replaces entity if same ID, merges facets by type+scope.
     *
     * @param resource resource to load from
     * @return number of entities loaded from this file
     * @throws IOException if file cannot be read
     */
    private int loadEntitiesFromResource(ResourceResolver.ResolvedResource resource) throws IOException {
        try (InputStream is = resource.inputStream()) {
            MetadataFileFormat fileFormat = yamlMapper.readValue(is, MetadataFileFormat.class);
            
            if (fileFormat.getEntities() == null || fileFormat.getEntities().isEmpty()) {
                return 0;
            }
            
            int count = 0;
            for (MetadataEntity incomingEntity : fileFormat.getEntities()) {
                if (incomingEntity.getCreatedAt() == null) {
                    incomingEntity.setCreatedAt(Instant.now());
                }
                if (incomingEntity.getUpdatedAt() == null) {
                    incomingEntity.setUpdatedAt(Instant.now());
                }
                if (incomingEntity.getFacets() == null) {
                    incomingEntity.setFacets(new HashMap<>());
                }
                
                String normalizedId = normalizeId(incomingEntity.getId());
                incomingEntity.setId(normalizedId);
                
                MetadataEntity existingEntity = entities.get(normalizedId);
                if (existingEntity != null) {
                    mergeEntityFacets(existingEntity, incomingEntity);
                    existingEntity.setType(incomingEntity.getType());
                    existingEntity.setSchemaName(incomingEntity.getSchemaName());
                    existingEntity.setTableName(incomingEntity.getTableName());
                    existingEntity.setAttributeName(incomingEntity.getAttributeName());
                    existingEntity.setUpdatedAt(incomingEntity.getUpdatedAt());
                    if (incomingEntity.getCreatedBy() != null) {
                        existingEntity.setCreatedBy(incomingEntity.getCreatedBy());
                    }
                    if (incomingEntity.getUpdatedBy() != null) {
                        existingEntity.setUpdatedBy(incomingEntity.getUpdatedBy());
                    }
                } else {
                    entities.put(normalizedId, incomingEntity);
                }
                count++;
            }
            
            return count;
        }
    }
    
    /**
     * Merge facets from incoming entity into existing entity.
     * Facets are replaced (not merged) when same facet type and scope exist.
     * Facets from existing entity that don't exist in incoming entity are preserved.
     *
     * @param existing existing entity (will be modified)
     * @param incoming incoming entity (source of new/updated facets)
     */
    private void mergeEntityFacets(MetadataEntity existing, MetadataEntity incoming) {
        if (incoming.getFacets() == null || incoming.getFacets().isEmpty()) {
            return;
        }
        
        if (existing.getFacets() == null) {
            existing.setFacets(new HashMap<>());
        }
        
        for (Map.Entry<String, Map<String, Object>> incomingFacetType : incoming.getFacets().entrySet()) {
            String facetType = incomingFacetType.getKey();
            Map<String, Object> incomingScopes = incomingFacetType.getValue();
            
            if (incomingScopes == null || incomingScopes.isEmpty()) {
                continue;
            }
            
            Map<String, Object> existingScopes = existing.getFacets().computeIfAbsent(facetType, k -> new HashMap<>());
            
            for (Map.Entry<String, Object> incomingScope : incomingScopes.entrySet()) {
                String scope = incomingScope.getKey();
                Object facetData = incomingScope.getValue();
                existingScopes.put(scope, facetData);
            }
        }
    }
    
    @Override
    public void save(MetadataEntity entity) {
        entity.setUpdatedAt(Instant.now());
        String normalizedId = normalizeId(entity.getId());
        entity.setId(normalizedId);
        entities.put(normalizedId, entity);
        log.debug("Saved entity: {}", normalizedId);
    }
    
    @Override
    public Optional<MetadataEntity> findById(String id) {
        return Optional.ofNullable(entities.get(normalizeId(id)));
    }
    
    @Override
    public Optional<MetadataEntity> findByLocation(
        String schema,
        String table,
        String attribute
    ) {
        return entities.values().stream()
            .filter(e -> Objects.equals(e.getSchemaName(), schema) &&
                        Objects.equals(e.getTableName(), table) &&
                        Objects.equals(e.getAttributeName(), attribute))
            .findFirst();
    }
    
    @Override
    public List<MetadataEntity> findByType(MetadataType type) {
        return entities.values().stream()
            .filter(e -> e.getType() == type)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<MetadataEntity> findAll() {
        return new ArrayList<>(entities.values());
    }
    
    @Override
    public void deleteById(String id) {
        String normalizedId = normalizeId(id);
        entities.remove(normalizedId);
        log.debug("Deleted entity: {}", normalizedId);
    }
    
    @Override
    public boolean existsById(String id) {
        return entities.containsKey(normalizeId(id));
    }
    
    /**
     * Normalize entity ID to lowercase for case-insensitive operations.
     *
     * @param id entity ID
     * @return normalized (lowercase) ID
     */
    private String normalizeId(String id) {
        return id != null ? id.toLowerCase() : null;
    }
    
    /**
     * YAML file format structure.
     */
    public static class MetadataFileFormat {
        @JsonProperty("entities")
        private List<MetadataEntity> entities;
        
        public List<MetadataEntity> getEntities() {
            return entities != null ? entities : List.of();
        }
        
        public void setEntities(List<MetadataEntity> entities) {
            this.entities = entities;
        }
    }
}
