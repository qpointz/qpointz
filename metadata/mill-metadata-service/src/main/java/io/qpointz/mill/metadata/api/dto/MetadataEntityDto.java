package io.qpointz.mill.metadata.api.dto;

import io.qpointz.mill.metadata.domain.MetadataType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for metadata entity API responses.
 */
@Data
@Builder
public class MetadataEntityDto {
    private String id;
    private MetadataType type;
    private String schemaName;
    private String tableName;
    private String attributeName;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Map<String, Object> facets;  // Simplified facets for API (merged for current user)
}

