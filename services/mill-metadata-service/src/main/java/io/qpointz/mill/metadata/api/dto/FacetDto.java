package io.qpointz.mill.metadata.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * DTO for facet API responses.
 */
@Data
@Builder
public class FacetDto {
    private String facetType;
    private Object data;  // Facet data (merged for current user)
    private Set<String> availableScopes;  // All scopes available for this facet
}

