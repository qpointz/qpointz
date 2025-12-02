package io.qpointz.mill.metadata.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for facets providing common functionality.
 */
public abstract class AbstractFacet implements MetadataFacet {
    
    @Getter
    @Setter
    private MetadataEntity owner;
    
    @Override
    public void validate() throws ValidationException {
        // Default: no validation. Subclasses can override.
    }
    
    @Override
    public MetadataFacet merge(MetadataFacet other) {
        // Default: return this (no merging). Subclasses can override.
        return this;
    }
}

