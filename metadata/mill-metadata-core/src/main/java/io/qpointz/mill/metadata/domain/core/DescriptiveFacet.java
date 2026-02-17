package io.qpointz.mill.metadata.domain.core;

import io.qpointz.mill.metadata.domain.AbstractFacet;
import io.qpointz.mill.metadata.domain.DataClassification;
import io.qpointz.mill.metadata.domain.MetadataFacet;
import io.qpointz.mill.metadata.domain.ValidationException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Descriptive facet - human-readable metadata.
 * Entity-bound facet attached to a single entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DescriptiveFacet extends AbstractFacet {
    
    private String displayName;
    private String description;
    private String businessMeaning;
    private List<String> synonyms = new ArrayList<>();
    private List<String> aliases = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private String businessDomain;
    private String businessOwner;  // Renamed from 'owner' to avoid conflict with AbstractFacet.owner
    private DataClassification classification;
    private String unit;  // USD, meters, etc.
    
    @Override
    public String getFacetType() {
        return "descriptive";
    }
    
    @Override
    public MetadataFacet merge(MetadataFacet other) {
        if (!(other instanceof DescriptiveFacet)) {
            return this;
        }
        DescriptiveFacet otherFacet = (DescriptiveFacet) other;
        
        // Merge: other takes precedence for most fields
        if (otherFacet.displayName != null) {
            this.displayName = otherFacet.displayName;
        }
        if (otherFacet.description != null) {
            this.description = otherFacet.description;
        }
        if (otherFacet.businessMeaning != null) {
            this.businessMeaning = otherFacet.businessMeaning;
        }
        if (otherFacet.businessDomain != null) {
            this.businessDomain = otherFacet.businessDomain;
        }
        if (otherFacet.businessOwner != null) {
            this.businessOwner = otherFacet.businessOwner;
        }
        if (otherFacet.classification != null) {
            this.classification = otherFacet.classification;
        }
        if (otherFacet.unit != null) {
            this.unit = otherFacet.unit;
        }
        
        // Merge lists (add unique items)
        if (otherFacet.synonyms != null && !otherFacet.synonyms.isEmpty()) {
            this.synonyms = new ArrayList<>(this.synonyms);
            otherFacet.synonyms.forEach(s -> {
                if (!this.synonyms.contains(s)) {
                    this.synonyms.add(s);
                }
            });
        }
        if (otherFacet.aliases != null && !otherFacet.aliases.isEmpty()) {
            this.aliases = new ArrayList<>(this.aliases);
            otherFacet.aliases.forEach(a -> {
                if (!this.aliases.contains(a)) {
                    this.aliases.add(a);
                }
            });
        }
        if (otherFacet.tags != null && !otherFacet.tags.isEmpty()) {
            this.tags = new ArrayList<>(this.tags);
            otherFacet.tags.forEach(t -> {
                if (!this.tags.contains(t)) {
                    this.tags.add(t);
                }
            });
        }
        
        return this;
    }
}

