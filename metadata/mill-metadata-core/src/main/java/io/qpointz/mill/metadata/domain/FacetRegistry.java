package io.qpointz.mill.metadata.domain;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for metadata facet types.
 * Enables dynamic facet discovery and serialization.
 */
@Slf4j
public class FacetRegistry {
    
    private static final FacetRegistry INSTANCE = new FacetRegistry();
    
    // Map facet type key to facet class
    private final Map<String, Class<? extends MetadataFacet>> facetTypes = new ConcurrentHashMap<>();
    
    private FacetRegistry() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance.
     *
     * @return registry instance
     */
    public static FacetRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a facet type.
     *
     * @param facetClass facet class
     */
    public void register(Class<? extends MetadataFacet> facetClass) {
        try {
            MetadataFacet instance = facetClass.getDeclaredConstructor().newInstance();
            String facetType = instance.getFacetType();
            facetTypes.put(facetType, facetClass);
            log.debug("Registered facet type: {} -> {}", facetType, facetClass.getName());
        } catch (Exception e) {
            log.error("Failed to register facet type: {}", facetClass.getName(), e);
            throw new RuntimeException("Failed to register facet type: " + facetClass.getName(), e);
        }
    }
    
    /**
     * Get facet type key for a facet class.
     *
     * @param facetClass facet class
     * @return facet type key
     */
    public static String getFacetType(Class<? extends MetadataFacet> facetClass) {
        try {
            MetadataFacet instance = facetClass.getDeclaredConstructor().newInstance();
            return instance.getFacetType();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get facet type for: " + facetClass.getName(), e);
        }
    }
    
    /**
     * Get facet class for a facet type key.
     *
     * @param facetType facet type key
     * @return facet class, or null if not registered
     */
    public Class<? extends MetadataFacet> getFacetClass(String facetType) {
        return facetTypes.get(facetType);
    }
    
    /**
     * Check if a facet type is registered.
     *
     * @param facetType facet type key
     * @return true if registered
     */
    public boolean isRegistered(String facetType) {
        return facetTypes.containsKey(facetType);
    }
}

