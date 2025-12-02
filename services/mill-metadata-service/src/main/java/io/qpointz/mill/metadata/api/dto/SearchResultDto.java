package io.qpointz.mill.metadata.api.dto;

import io.qpointz.mill.metadata.domain.MetadataType;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for search results.
 */
@Data
@Builder
public class SearchResultDto {
    private String id;
    private String name;
    private MetadataType type;
    private String displayName;
    private String description;
    private String location;  // FQN or location string
    private Double score;  // Relevance score if applicable
}

