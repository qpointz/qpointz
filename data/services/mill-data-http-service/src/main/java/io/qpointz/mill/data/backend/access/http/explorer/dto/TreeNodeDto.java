package io.qpointz.mill.data.backend.access.http.explorer.dto;

import io.qpointz.mill.metadata.domain.MetadataType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TreeNodeDto {
    private String id;
    private String name;
    private MetadataType type;
    private String displayName;
    private String description;
    private List<TreeNodeDto> children;
    private boolean hasChildren;
}
