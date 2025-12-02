package io.qpointz.mill.metadata.domain;

/**
 * Base interface for all metadata facets.
 * A facet represents one aspect or dimension of metadata.
 */
public interface MetadataFacet {
    
    /**
     * Get the facet type identifier (e.g., "descriptive", "structural", "value-mapping").
     * This is used as the key in the facets map.
     *
     * @return facet type identifier
     */
    String getFacetType();
    
    /**
     * Set the owner entity (called when facet is added to an entity).
     *
     * @param owner the metadata entity that owns this facet
     */
    void setOwner(MetadataEntity owner);
    
    /**
     * Validate the facet data.
     *
     * @throws ValidationException if validation fails
     */
    void validate() throws ValidationException;
    
    /**
     * Merge this facet with another facet of the same type.
     * Used when merging facets from different scopes.
     *
     * @param other the other facet to merge with
     * @return merged facet (typically this instance with other's data merged in)
     */
    MetadataFacet merge(MetadataFacet other);
}

