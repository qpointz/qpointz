package io.qpointz.mill.data.backend.access.http.explorer.dto;

import io.qpointz.mill.metadata.domain.MetadataType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResultDto {
    private String id;
    private String name;
    private MetadataType type;
    private String displayName;
    private String description;
    private String location;
    private Double score;
}
