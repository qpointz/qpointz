package io.qpointz.mill.metadata.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private MetadataType type;

    private String schemaName;
    private String tableName;
    private String attributeName;

    @JsonProperty("facets")
    private Map<String, Map<String, Object>> facets = new HashMap<>();

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @JsonIgnore
    private transient FacetConverter facetConverter;

    private FacetConverter converter() {
        return facetConverter != null ? facetConverter : FacetConverter.defaultConverter();
    }

    public <T> Optional<T> getFacet(String facetType, String scope, Class<T> facetClass) {
        Map<String, Object> scopedFacets = facets.get(facetType);
        if (scopedFacets == null) {
            return Optional.empty();
        }
        Object facetData = scopedFacets.get(scope);
        return converter().convert(facetData, facetClass);
    }

    public Object getRawFacet(String facetType, String scope) {
        Map<String, Object> scopedFacets = facets.get(facetType);
        if (scopedFacets == null) {
            return null;
        }
        return scopedFacets.get(scope);
    }

    public Set<String> getFacetScopes(String facetType) {
        Map<String, Object> scopedFacets = facets.get(facetType);
        return scopedFacets != null ? scopedFacets.keySet() : Set.of();
    }

    public void setFacet(String facetType, String scope, Object facetData) {
        facets.computeIfAbsent(facetType, k -> new HashMap<>())
              .put(scope, facetData);
    }

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

        List<Object> facetDataList = new ArrayList<>();

        if (scopedFacets.containsKey("global")) {
            facetDataList.add(scopedFacets.get("global"));
        }

        for (String role : userRoles) {
            String roleScope = "role:" + role;
            if (scopedFacets.containsKey(roleScope)) {
                facetDataList.add(scopedFacets.get(roleScope));
            }
        }

        for (String team : userTeams) {
            String teamScope = "team:" + team;
            if (scopedFacets.containsKey(teamScope)) {
                facetDataList.add(scopedFacets.get(teamScope));
            }
        }

        String userScope = "user:" + userId;
        if (scopedFacets.containsKey(userScope)) {
            facetDataList.add(scopedFacets.get(userScope));
        }

        if (facetDataList.isEmpty()) {
            return Optional.empty();
        }

        Object merged = facetDataList.get(facetDataList.size() - 1);
        return converter().convert(merged, facetClass);
    }
}
