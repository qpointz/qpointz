package io.qpointz.mill.metadata.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacetTypeDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String typeKey;
    @Builder.Default
    private boolean mandatory = false;
    @Builder.Default
    private boolean enabled = true;
    private String displayName;
    private String description;
    private Set<MetadataTargetType> applicableTo;
    private String version;
    private Map<String, Object> contentSchema;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public boolean isApplicableTo(MetadataTargetType targetType) {
        if (applicableTo == null || applicableTo.isEmpty() || applicableTo.contains(MetadataTargetType.ANY)) {
            return true;
        }
        return applicableTo.contains(targetType);
    }

    public boolean hasContentSchema() {
        return contentSchema != null && !contentSchema.isEmpty();
    }
}
