package io.qpointz.mill.metadata.domain.core;

import io.qpointz.mill.metadata.domain.AbstractFacet;
import io.qpointz.mill.metadata.domain.MetadataFacet;
import io.qpointz.mill.metadata.domain.RelationCardinality;
import io.qpointz.mill.metadata.domain.RelationType;
import io.qpointz.mill.metadata.domain.ValidationException;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Relation facet - relationships between entities.
 * Cross-entity facet stored on source entity, references target entities.
 */
@EqualsAndHashCode(callSuper = true)
public class RelationFacet extends AbstractFacet {
    
    private List<Relation> relations = new ArrayList<>();
    
    /**
     * Relation between two entities.
     */
    public record Relation(
        String name,
        String description,
        EntityReference sourceTable,
        List<String> sourceAttributes,
        EntityReference targetTable,
        List<String> targetAttributes,
        RelationCardinality cardinality,
        RelationType type,
        String joinSql,
        String businessMeaning
    ) {
        /**
         * Constructor with default empty lists.
         */
        public Relation {
            if (sourceAttributes == null) {
                sourceAttributes = new ArrayList<>();
            }
            if (targetAttributes == null) {
                targetAttributes = new ArrayList<>();
            }
        }
    }
    
    @Override
    public String getFacetType() {
        return "relation";
    }
    
    @Override
    public void validate() throws ValidationException {
        if (relations != null) {
            for (Relation relation : relations) {
                if (relation.name() == null || relation.name().isEmpty()) {
                    throw new ValidationException("RelationFacet: relation name is required");
                }
                if (relation.sourceTable() == null) {
                    throw new ValidationException("RelationFacet: sourceTable is required for relation: " + relation.name());
                }
                if (relation.targetTable() == null) {
                    throw new ValidationException("RelationFacet: targetTable is required for relation: " + relation.name());
                }
            }
        }
    }
    
    @Override
    public MetadataFacet merge(MetadataFacet other) {
        if (!(other instanceof RelationFacet)) {
            return this;
        }
        RelationFacet otherFacet = (RelationFacet) other;
        
        // Merge relations: combine lists, avoid duplicates by name
        if (otherFacet.relations != null && !otherFacet.relations.isEmpty()) {
            this.relations = new ArrayList<>(this.relations);
            for (Relation otherRelation : otherFacet.relations) {
                boolean exists = this.relations.stream()
                    .anyMatch(r -> r.name().equals(otherRelation.name()));
                if (!exists) {
                    this.relations.add(otherRelation);
                }
            }
        }
        
        return this;
    }
    
    /**
     * Get relations where this entity participates (as source or target).
     *
     * @param schema schema name
     * @param table table name
     * @return list of matching relations
     */
    public List<Relation> getRelationsForEntity(String schema, String table) {
        return relations.stream()
            .filter(r -> r.sourceTable().matches(schema, table, null) ||
                        r.targetTable().matches(schema, table, null))
            .toList();
    }
}

