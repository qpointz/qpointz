package io.qpointz.mill.metadata.api.dto;

import io.qpointz.mill.metadata.domain.MetadataType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for tree navigation responses.
 */
@Data
@Builder
public class TreeNodeDto {
    private String id;
    private String name;
    private MetadataType type;
    private String displayName;
    private String description;
    private List<TreeNodeDto> children;
    private boolean hasChildren;  // Hint for lazy loading
}

