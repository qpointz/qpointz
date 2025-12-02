package io.qpointz.mill.metadata.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.*;

/**
 * Represents a metadata entity (schema, table, attribute, or concept).
 * Uses document-style persistence with facets stored as JSON.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataEntity {
    
    private String id;
    private MetadataType type;
    
    // Hierarchical location (nullable for unbound entities like CONCEPT)
    private String schemaName;
    private String tableName;
    private String attributeName;
    
    // Facets: Map<facetType, Map<scope, facetData>>
    // Structure: { "descriptive": { "global": {...}, "user:alice": {...} } }
    @JsonProperty("facets")
    private Map<String, Map<String, Object>> facets = new HashMap<>();
    
    // Audit
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    
    /**
     * Get facet data for a specific scope.
     *
     * @param facetType facet type (e.g., "descriptive", "structural")
     * @param scope scope (e.g., "global", "user:alice@company.com")
     * @param facetClass facet class to deserialize to
     * @return optional facet data
     */
    public <T> Optional<T> getFacet(String facetType, String scope, Class<T> facetClass) {
        Map<String, Object> scopedFacets = facets.get(facetType);
        if (scopedFacets == null) {
            return Optional.empty();
        }
        Object facetData = scopedFacets.get(scope);
        if (facetData == null) {
            return Optional.empty();
        }
        // For now, return as-is. Jackson will handle deserialization.
        // In a full implementation, we'd use ObjectMapper to convert.
        return Optional.of(facetClass.cast(facetData));
    }
    
    /**
     * Get all scopes for a facet type.
     *
     * @param facetType facet type
     * @return set of scope names
     */
    public Set<String> getFacetScopes(String facetType) {
        Map<String, Object> scopedFacets = facets.get(facetType);
        return scopedFacets != null ? scopedFacets.keySet() : Set.of();
    }
    
    /**
     * Set facet data for a specific scope.
     *
     * @param facetType facet type
     * @param scope scope
     * @param facetData facet data (will be serialized to JSON)
     */
    public void setFacet(String facetType, String scope, Object facetData) {
        facets.computeIfAbsent(facetType, k -> new HashMap<>())
              .put(scope, facetData);
    }
    
    /**
     * Get merged facet data for a user (global + user-specific + team + role).
     * Merges in priority: user > team > role > global.
     *
     * @param facetType facet type
     * @param userId user ID
     * @param userTeams list of team names user belongs to
     * @param userRoles list of role names user has
     * @param facetClass facet class
     * @return optional merged facet
     */
    public <T> Optional<T> getMergedFacet(
        String facetType,
        String userId,
        List<String> userTeams,
        List<String> userRoles,
        Class<T> facetClass
    ) {
        Map<String, Object> scopedFacets = facets.get(facetType);
        if (scopedFacets == null) {
            return Optional.empty();
        }
        
        // Collect facets in priority order (lowest to highest)
        List<Object> facetDataList = new ArrayList<>();
        
        // 1. Global (lowest priority)
        if (scopedFacets.containsKey("global")) {
            facetDataList.add(scopedFacets.get("global"));
        }
        
        // 2. Role (higher priority)
        for (String role : userRoles) {
            String roleScope = "role:" + role;
            if (scopedFacets.containsKey(roleScope)) {
                facetDataList.add(scopedFacets.get(roleScope));
            }
        }
        
        // 3. Team (higher priority)
        for (String team : userTeams) {
            String teamScope = "team:" + team;
            if (scopedFacets.containsKey(teamScope)) {
                facetDataList.add(scopedFacets.get(teamScope));
            }
        }
        
        // 4. User (highest priority)
        String userScope = "user:" + userId;
        if (scopedFacets.containsKey(userScope)) {
            facetDataList.add(scopedFacets.get(userScope));
        }
        
        if (facetDataList.isEmpty()) {
            return Optional.empty();
        }
        
        // For now, return the highest priority facet (last in list).
        // Full implementation would merge facet objects.
        Object merged = facetDataList.get(facetDataList.size() - 1);
        return Optional.of(facetClass.cast(merged));
    }
}

