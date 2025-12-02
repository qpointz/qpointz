package io.qpointz.mill.metadata.repository;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for metadata entities.
 * Supports document-style persistence with JSON facets.
 */
public interface MetadataRepository {
    
    /**
     * Save a metadata entity (create or update).
     *
     * @param entity entity to save
     */
    void save(MetadataEntity entity);
    
    /**
     * Find entity by ID.
     *
     * @param id entity ID
     * @return optional entity
     */
    Optional<MetadataEntity> findById(String id);
    
    /**
     * Find entity by hierarchical location.
     *
     * @param schema schema name
     * @param table table name (null for schema-level entities)
     * @param attribute attribute name (null for table-level entities)
     * @return optional entity
     */
    Optional<MetadataEntity> findByLocation(
        String schema,
        String table,
        String attribute
    );
    
    /**
     * Find all entities of a specific type.
     *
     * @param type metadata type
     * @return list of entities
     */
    List<MetadataEntity> findByType(MetadataType type);
    
    /**
     * Find all entities.
     *
     * @return list of all entities
     */
    List<MetadataEntity> findAll();
    
    /**
     * Delete entity by ID.
     *
     * @param id entity ID
     */
    void deleteById(String id);
    
    /**
     * Check if entity exists.
     *
     * @param id entity ID
     * @return true if exists
     */
    boolean existsById(String id);
}

