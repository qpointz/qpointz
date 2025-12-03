package io.qpointz.mill.metadata.domain.core;

import io.qpointz.mill.metadata.domain.AbstractFacet;
import io.qpointz.mill.metadata.domain.ConceptSource;
import io.qpointz.mill.metadata.domain.MetadataFacet;
import io.qpointz.mill.metadata.domain.ValidationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Concept facet - business concepts spanning multiple entities.
 * Unbound facet stored as standalone CONCEPT entity.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class ConceptFacet extends AbstractFacet {
    
    private List<Concept> concepts = new ArrayList<>();
    
    /**
     * Business concept definition.
     */
    public record Concept(
        String name,
        String description,
        String sql,  // SQL definition (optional)
        List<ConceptTarget> targets,  // List of tables with optional attributes
        List<String> tags,
        String category,
        ConceptSource source,
        String sourceSession  // NL2SQL session ID if applicable
    ) {
        /**
         * Constructor with default empty lists.
         */
        public Concept {
            if (targets == null) {
                targets = new ArrayList<>();
            }
            if (tags == null) {
                tags = new ArrayList<>();
            }
        }
    }
    
    @Override
    public String getFacetType() {
        return "concept";
    }
    
    @Override
    public void validate() throws ValidationException {
        if (concepts != null) {
            for (Concept concept : concepts) {
                if (concept.name() == null || concept.name().isEmpty()) {
                    throw new ValidationException("ConceptFacet: concept name is required");
                }
            }
        }
    }
    
    @Override
    public MetadataFacet merge(MetadataFacet other) {
        if (!(other instanceof ConceptFacet)) {
            return this;
        }
        ConceptFacet otherFacet = (ConceptFacet) other;
        
        // Merge concepts: combine lists, avoid duplicates by name
        if (otherFacet.concepts != null && !otherFacet.concepts.isEmpty()) {
            this.concepts = new ArrayList<>(this.concepts);
            for (Concept otherConcept : otherFacet.concepts) {
                boolean exists = this.concepts.stream()
                    .anyMatch(c -> c.name().equals(otherConcept.name()));
                if (!exists) {
                    this.concepts.add(otherConcept);
                }
            }
        }
        
        return this;
    }
}

