package io.qpointz.mill.metadata.service;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.domain.core.ConceptFacet;
import io.qpointz.mill.metadata.domain.core.ConceptTarget;
import io.qpointz.mill.metadata.domain.core.EntityReference;
import io.qpointz.mill.metadata.domain.core.RelationFacet;
import io.qpointz.mill.metadata.repository.MetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service layer for metadata operations.
 * Provides scope-aware facet access and entity management.
 */
@Slf4j
@RequiredArgsConstructor
public class MetadataService {
    
    private final MetadataRepository repository;
    
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
    
    /**
     * Find all entities related to the given entity (bidirectional).
     * 
     * Finds:
     * 1. Entities that reference the selected entity (e.g., concepts that target this table)
     * 2. Entities that the selected entity references (e.g., tables referenced in relations)
     *
     * @param entityId entity ID
     * @param scope scope for facet access
     * @return list of related entities (deduplicated)
     */
    public List<MetadataEntity> findRelatedEntities(String entityId, String scope) {
        Optional<MetadataEntity> selectedEntityOpt = repository.findById(entityId);
        if (selectedEntityOpt.isEmpty()) {
            return List.of();
        }
        
        MetadataEntity selectedEntity = selectedEntityOpt.get();
        Set<String> relatedEntityIds = new HashSet<>();
        
        // 1. Find entities that reference the selected entity
        
        // 1a. Find concepts that reference this entity
        List<MetadataEntity> allConcepts = repository.findByType(MetadataType.CONCEPT);
        for (MetadataEntity concept : allConcepts) {
            Optional<ConceptFacet> conceptFacetOpt = concept.getFacet("concept", scope, ConceptFacet.class);
            if (conceptFacetOpt.isPresent()) {
                ConceptFacet conceptFacet = conceptFacetOpt.get();
                for (ConceptFacet.Concept c : conceptFacet.getConcepts()) {
                    if (c.targets() != null) {
                        for (ConceptTarget target : c.targets()) {
                            if (matchesEntity(selectedEntity, target.schema(), target.table(), null)) {
                                relatedEntityIds.add(concept.getId());
                                break;
                            }
                            // Check if any attribute matches
                            if (selectedEntity.getAttributeName() != null && 
                                target.attributes() != null &&
                                target.attributes().contains(selectedEntity.getAttributeName())) {
                                relatedEntityIds.add(concept.getId());
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // 1b. Find relations where this entity is source or target
        List<MetadataEntity> allEntities = repository.findAll();
        for (MetadataEntity entity : allEntities) {
            Optional<RelationFacet> relationFacetOpt = entity.getFacet("relation", scope, RelationFacet.class);
            if (relationFacetOpt.isPresent()) {
                RelationFacet relationFacet = relationFacetOpt.get();
                for (RelationFacet.Relation relation : relationFacet.getRelations()) {
                    // Check if selected entity is source or target
                    if (matchesEntityReference(selectedEntity, relation.sourceTable()) ||
                        matchesEntityReference(selectedEntity, relation.targetTable())) {
                        // Add the entity that has this relation (source entity)
                        relatedEntityIds.add(entity.getId());
                        // Also add the target entity if it's different
                        if (relation.targetTable() != null) {
                            String targetId = buildEntityId(relation.targetTable());
                            if (targetId != null && !targetId.equals(entityId)) {
                                repository.findById(targetId).ifPresent(e -> relatedEntityIds.add(e.getId()));
                            }
                        }
                    }
                }
            }
        }
        
        // 2. Find entities that the selected entity references
        
        // 2a. If selected entity has RelationFacet, extract target references
        Optional<RelationFacet> selectedRelationFacetOpt = selectedEntity.getFacet("relation", scope, RelationFacet.class);
        if (selectedRelationFacetOpt.isPresent()) {
            RelationFacet selectedRelationFacet = selectedRelationFacetOpt.get();
            for (RelationFacet.Relation relation : selectedRelationFacet.getRelations()) {
                if (relation.targetTable() != null) {
                    String targetId = buildEntityId(relation.targetTable());
                    if (targetId != null && !targetId.equals(entityId)) {
                        repository.findById(targetId).ifPresent(e -> relatedEntityIds.add(e.getId()));
                    }
                }
            }
        }
        
        // 2b. If selected entity has ConceptFacet, extract target references
        Optional<ConceptFacet> selectedConceptFacetOpt = selectedEntity.getFacet("concept", scope, ConceptFacet.class);
        if (selectedConceptFacetOpt.isPresent()) {
            ConceptFacet selectedConceptFacet = selectedConceptFacetOpt.get();
            for (ConceptFacet.Concept concept : selectedConceptFacet.getConcepts()) {
                if (concept.targets() != null) {
                    for (ConceptTarget target : concept.targets()) {
                        Optional<MetadataEntity> targetEntity = repository.findByLocation(
                            target.schema(), target.table(), null);
                        targetEntity.ifPresent(e -> relatedEntityIds.add(e.getId()));
                        
                        // Also find attribute entities if specified
                        if (target.attributes() != null) {
                            for (String attr : target.attributes()) {
                                repository.findByLocation(target.schema(), target.table(), attr)
                                    .ifPresent(e -> relatedEntityIds.add(e.getId()));
                            }
                        }
                    }
                }
            }
        }
        
        // Return deduplicated list of related entities
        return relatedEntityIds.stream()
            .map(repository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(e -> !e.getId().equals(entityId)) // Exclude self
            .collect(Collectors.toList());
    }
    
    /**
     * Check if an entity matches the given location.
     */
    private boolean matchesEntity(MetadataEntity entity, String schema, String table, String attribute) {
        return Objects.equals(entity.getSchemaName(), schema) &&
               Objects.equals(entity.getTableName(), table) &&
               (attribute == null || Objects.equals(entity.getAttributeName(), attribute));
    }
    
    /**
     * Check if an entity matches an EntityReference.
     */
    private boolean matchesEntityReference(MetadataEntity entity, EntityReference ref) {
        if (ref == null) return false;
        return ref.matches(entity.getSchemaName(), entity.getTableName(), entity.getAttributeName());
    }
    
    /**
     * Build entity ID from EntityReference.
     */
    private String buildEntityId(EntityReference ref) {
        if (ref == null) return null;
        if (ref.attribute() != null && !ref.attribute().isEmpty()) {
            return String.format("%s.%s.%s", ref.schema(), ref.table(), ref.attribute());
        }
        return String.format("%s.%s", ref.schema(), ref.table());
    }
}

