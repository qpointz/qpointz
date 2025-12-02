package io.qpointz.mill.metadata.repository.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.repository.MetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * File-based metadata repository using YAML format.
 * Reads entities from YAML file with document-style structure.
 */
@Slf4j
public class FileMetadataRepository implements MetadataRepository {
    
    private final Map<String, MetadataEntity> entities = new ConcurrentHashMap<>();
    private final ObjectMapper yamlMapper;
    private final ResourceLoader resourceLoader;
    private final String location;
    
    public FileMetadataRepository(String location, ResourceLoader resourceLoader) {
        this.location = location;
        this.resourceLoader = resourceLoader;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
        loadEntities();
    }
    
    private void loadEntities() {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                log.warn("Metadata file not found: {}", location);
                return;
            }
            
            try (InputStream is = resource.getInputStream()) {
                MetadataFileFormat fileFormat = yamlMapper.readValue(is, MetadataFileFormat.class);
                
                if (fileFormat.getEntities() != null) {
                    for (MetadataEntity entity : fileFormat.getEntities()) {
                        // Set defaults
                        if (entity.getCreatedAt() == null) {
                            entity.setCreatedAt(Instant.now());
                        }
                        if (entity.getUpdatedAt() == null) {
                            entity.setUpdatedAt(Instant.now());
                        }
                        if (entity.getFacets() == null) {
                            entity.setFacets(new HashMap<>());
                        }
                        
                        entities.put(entity.getId(), entity);
                    }
                    log.info("Loaded {} metadata entities from {}", entities.size(), location);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load metadata from file: {}", location, e);
            throw new RuntimeException("Failed to load metadata from file: " + location, e);
        }
    }
    
    @Override
    public void save(MetadataEntity entity) {
        entity.setUpdatedAt(Instant.now());
        entities.put(entity.getId(), entity);
        // Note: In a full implementation, we'd write back to file
        log.debug("Saved entity: {}", entity.getId());
    }
    
    @Override
    public Optional<MetadataEntity> findById(String id) {
        return Optional.ofNullable(entities.get(id));
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
        entities.remove(id);
        log.debug("Deleted entity: {}", id);
    }
    
    @Override
    public boolean existsById(String id) {
        return entities.containsKey(id);
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

