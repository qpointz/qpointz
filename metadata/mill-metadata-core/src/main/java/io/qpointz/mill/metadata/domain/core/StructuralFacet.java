package io.qpointz.mill.metadata.domain.core;

import io.qpointz.mill.metadata.domain.AbstractFacet;
import io.qpointz.mill.metadata.domain.MetadataFacet;
import io.qpointz.mill.metadata.domain.ValidationException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Structural facet - physical schema binding.
 * Entity-bound facet attached to a single entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StructuralFacet extends AbstractFacet {
    
    private String physicalName;
    private String physicalType;  // VARCHAR, INTEGER, etc.
    private Integer precision;
    private Integer scale;
    private Boolean nullable;
    private Boolean isPrimaryKey;
    private Boolean isForeignKey;
    private Boolean isUnique;
    private String backendType;  // jdbc, calcite, etc.
    private TableType tableType;  // For tables
    private Instant lastSyncedAt;
    
    @Override
    public String getFacetType() {
        return "structural";
    }
    
    @Override
    public void validate() throws ValidationException {
        if (physicalName == null || physicalName.isEmpty()) {
            throw new ValidationException("StructuralFacet: physicalName is required");
        }
    }
    
    @Override
    public MetadataFacet merge(MetadataFacet other) {
        if (!(other instanceof StructuralFacet)) {
            return this;
        }
        StructuralFacet otherFacet = (StructuralFacet) other;
        
        // Merge: other takes precedence for most fields
        if (otherFacet.physicalName != null) {
            this.physicalName = otherFacet.physicalName;
        }
        if (otherFacet.physicalType != null) {
            this.physicalType = otherFacet.physicalType;
        }
        if (otherFacet.precision != null) {
            this.precision = otherFacet.precision;
        }
        if (otherFacet.scale != null) {
            this.scale = otherFacet.scale;
        }
        if (otherFacet.nullable != null) {
            this.nullable = otherFacet.nullable;
        }
        if (otherFacet.isPrimaryKey != null) {
            this.isPrimaryKey = otherFacet.isPrimaryKey;
        }
        if (otherFacet.isForeignKey != null) {
            this.isForeignKey = otherFacet.isForeignKey;
        }
        if (otherFacet.isUnique != null) {
            this.isUnique = otherFacet.isUnique;
        }
        if (otherFacet.backendType != null) {
            this.backendType = otherFacet.backendType;
        }
        if (otherFacet.tableType != null) {
            this.tableType = otherFacet.tableType;
        }
        if (otherFacet.lastSyncedAt != null) {
            this.lastSyncedAt = otherFacet.lastSyncedAt;
        }
        
        return this;
    }
}

