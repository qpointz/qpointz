package io.qpointz.mill.metadata.api;

import io.qpointz.mill.metadata.api.dto.MetadataEntityDto;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper between domain entities and DTOs.
 */
@Component
public class DtoMapper {
    
    /**
     * Convert domain entity to DTO with merged facets for scope.
     */
    public MetadataEntityDto toDto(MetadataEntity entity, String scope) {
        // Get merged facets for the specified scope
        Map<String, Object> mergedFacets = new HashMap<>();
        for (String facetType : entity.getFacets().keySet()) {
            entity.getFacet(facetType, scope, Object.class)
                .ifPresent(data -> mergedFacets.put(facetType, data));
        }
        
        return MetadataEntityDto.builder()
            .id(entity.getId())
            .type(entity.getType())
            .schemaName(entity.getSchemaName())
            .tableName(entity.getTableName())
            .attributeName(entity.getAttributeName())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .facets(mergedFacets)
            .build();
    }
    
    /**
     * Convert DTO to domain entity.
     */
    public MetadataEntity toEntity(MetadataEntityDto dto) {
        MetadataEntity entity = new MetadataEntity();
        entity.setId(dto.getId());
        entity.setType(dto.getType());
        entity.setSchemaName(dto.getSchemaName());
        entity.setTableName(dto.getTableName());
        entity.setAttributeName(dto.getAttributeName());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        
        // Convert facets map back to scoped structure
        if (dto.getFacets() != null) {
            Map<String, Map<String, Object>> scopedFacets = new HashMap<>();
            dto.getFacets().forEach((facetType, data) -> {
                Map<String, Object> scopeMap = new HashMap<>();
                scopeMap.put("global", data);
                scopedFacets.put(facetType, scopeMap);
            });
            entity.setFacets(scopedFacets);
        }
        
        return entity;
    }
}

