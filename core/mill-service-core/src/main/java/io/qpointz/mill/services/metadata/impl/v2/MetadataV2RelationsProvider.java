package io.qpointz.mill.services.metadata.impl.v2;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.RelationCardinality;
import io.qpointz.mill.metadata.domain.core.EntityReference;
import io.qpointz.mill.metadata.domain.core.RelationFacet;
import io.qpointz.mill.metadata.service.MetadataService;
import io.qpointz.mill.services.metadata.RelationsProvider;
import io.qpointz.mill.services.metadata.model.Relation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Lazy
@ConditionalOnProperty(prefix = "mill.metadata", name = "relations", havingValue = "v2")
public class MetadataV2RelationsProvider  implements RelationsProvider {


    private final MetadataService metadataService;

    public MetadataV2RelationsProvider(MetadataService repository) {
        log.info("Using metadata v2 relations provider");
        this.metadataService = repository;
    }

    @Override
    public Collection<Relation> getRelations() {
        List<Relation> result = new ArrayList<>();
        
        List<MetadataEntity> allEntities = metadataService.findAll();
        for (MetadataEntity entity : allEntities) {
            Optional<RelationFacet> relationFacetOpt = entity.getFacet("relation", "global", RelationFacet.class);
            if (relationFacetOpt.isPresent()) {
                RelationFacet relationFacet = relationFacetOpt.get();
                for (RelationFacet.Relation rel : relationFacet.getRelations()) {
                    try {
                        Relation mappedRelation = mapRelation(rel);
                        result.add(mappedRelation);
                    } catch (UnsupportedOperationException e) {
                        log.warn("Skipping relation '{}' from entity '{}': {}", 
                                rel.name(), entity.getId(), e.getMessage());
                    }
                }
            }
        }
        
        return result;
    }

    private Relation mapRelation(RelationFacet.Relation rel) {
        EntityReference sourceTable = rel.sourceTable();
        EntityReference targetTable = rel.targetTable();
        
        String sourceAttribute = extractAttribute(rel.sourceAttributes(), sourceTable, "source");
        String targetAttribute = extractAttribute(rel.targetAttributes(), targetTable, "target");
        
        Relation.TableRef source = new Relation.TableRef(
                sourceTable.schema(),
                sourceTable.table()
        );
        
        Relation.TableRef target = new Relation.TableRef(
                targetTable.schema(),
                targetTable.table()
        );
        
        Relation.AttributeRelation attributeRelation = new Relation.AttributeRelation(
                new Relation.AttributeRef(sourceAttribute),
                new Relation.AttributeRef(targetAttribute)
        );
        
        Relation.Cardinality cardinality = mapCardinality(rel.cardinality());
        
        Optional<String> description = Optional.ofNullable(rel.description());
        
        return new Relation(source, target, attributeRelation, cardinality, description);
    }

    private String extractAttribute(List<String> attributes, EntityReference ref, String context) {
        if (attributes == null || attributes.isEmpty()) {
            // Fall back to attribute from EntityReference if available
            return ref.attribute() != null ? ref.attribute() : null;
        }
        
        if (attributes.size() > 1) {
            throw new UnsupportedOperationException(
                    String.format("Multiple %s attributes not yet supported", context)
            );
        }
        
        return attributes.get(0);
    }

    private Relation.Cardinality mapCardinality(RelationCardinality cardinality) {
        if (cardinality == null) {
            return Relation.Cardinality.UNSPECIFIED;
        }
        
        return switch (cardinality) {
            case ONE_TO_ONE -> Relation.Cardinality.ONE_TO_ONE;
            case ONE_TO_MANY -> Relation.Cardinality.ONE_TO_MANY;
            case MANY_TO_ONE -> Relation.Cardinality.ONE_TO_MANY; // Reverse direction
            case MANY_TO_MANY -> Relation.Cardinality.MANY_TO_MANY;
        };
    }
}
