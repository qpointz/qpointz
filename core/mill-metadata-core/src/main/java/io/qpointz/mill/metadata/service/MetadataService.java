package io.qpointz.mill.metadata.service;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.repository.MetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for metadata operations.
 * Provides scope-aware facet access and entity management.
 */
@Slf4j
@Service
//@ConditionalOnBean(MetadataRepository.class)
//@RequiredArgsConstructor
public class MetadataService {
    
    private final MetadataRepository repository;

    public MetadataService(@Autowired(required = false) MetadataRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Get entity by ID.
     *
     * @param id entity ID
     * @return optional entity
     */
    public Optional<MetadataEntity> findById(String id) {
        return repository.findById(id);
    }
    
    /**
     * Get entity by location.
     *
     * @param schema schema name
     * @param table table name
     * @param attribute attribute name
     * @return optional entity
     */
    public Optional<MetadataEntity> findByLocation(
        String schema,
        String table,
        String attribute
    ) {
        return repository.findByLocation(schema, table, attribute);
    }
    
    /**
     * Get entities by type.
     *
     * @param type metadata type
     * @return list of entities
     */
    public List<MetadataEntity> findByType(MetadataType type) {
        return repository.findByType(type);
    }
    
    /**
     * Get all entities.
     *
     * @return list of all entities
     */
    public List<MetadataEntity> findAll() {
        return repository.findAll();
    }
    
    /**
     * Save entity.
     *
     * @param entity entity to save
     */
    public void save(MetadataEntity entity) {
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
    }
    
    /**
     * Delete entity by ID.
     *
     * @param id entity ID
     */
    public void deleteById(String id) {
        repository.deleteById(id);
    }
    
    /**
     * Get facet for specific scope.
     *
     * @param entityId entity ID
     * @param facetType facet type
     * @param scope scope
     * @param facetClass facet class
     * @return optional facet data
     */
    public <T> Optional<T> getFacet(
        String entityId,
        String facetType,
        String scope,
        Class<T> facetClass
    ) {
        return repository.findById(entityId)
            .flatMap(entity -> entity.getFacet(facetType, scope, facetClass));
    }
    
    /**
     * Get merged facet for current user (global + user + team + role).
     * Basic implementation - returns global scope for now.
     *
     * @param entityId entity ID
     * @param facetType facet type
     * @param userId user ID
     * @param userTeams user teams
     * @param userRoles user roles
     * @param facetClass facet class
     * @return optional merged facet
     */
    public <T> Optional<T> getMergedFacet(
        String entityId,
        String facetType,
        String userId,
        List<String> userTeams,
        List<String> userRoles,
        Class<T> facetClass
    ) {
        return repository.findById(entityId)
            .flatMap(entity -> entity.getMergedFacet(
                facetType,
                userId,
                userTeams != null ? userTeams : List.of(),
                userRoles != null ? userRoles : List.of(),
                facetClass
            ));
    }
    
    /**
     * Set facet for specific scope.
     *
     * @param entityId entity ID
     * @param facetType facet type
     * @param scope scope
     * @param facetData facet data
     */
    public void setFacet(
        String entityId,
        String facetType,
        String scope,
        Object facetData
    ) {
        MetadataEntity entity = repository.findById(entityId)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));
        
        entity.setFacet(facetType, scope, facetData);
        save(entity);
    }
}

