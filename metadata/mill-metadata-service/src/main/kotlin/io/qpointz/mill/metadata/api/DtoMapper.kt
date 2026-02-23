package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.MetadataEntityDto
import io.qpointz.mill.metadata.domain.MetadataEntity
import org.springframework.stereotype.Component

/** Maps domain entities to REST DTOs and back. */
@Component
class DtoMapper {

    /** Converts domain entity to scope-resolved API DTO. */
    fun toDto(entity: MetadataEntity, scope: String): MetadataEntityDto {
        val mergedFacets = mutableMapOf<String, Any?>()
        for (facetType in entity.facets.keys) {
            entity.getFacet(facetType, scope, Any::class.java)
                .ifPresent { mergedFacets[facetType] = it }
        }
        return MetadataEntityDto(
            id = entity.id,
            type = entity.type,
            schemaName = entity.schemaName,
            tableName = entity.tableName,
            attributeName = entity.attributeName,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            createdBy = entity.createdBy,
            updatedBy = entity.updatedBy,
            facets = mergedFacets
        )
    }

    /** Converts API DTO into domain entity with default `global` facet scope. */
    fun toEntity(dto: MetadataEntityDto): MetadataEntity {
        val entity = MetadataEntity(
            id = dto.id,
            type = dto.type,
            schemaName = dto.schemaName,
            tableName = dto.tableName,
            attributeName = dto.attributeName,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            createdBy = dto.createdBy,
            updatedBy = dto.updatedBy
        )
        dto.facets?.forEach { (facetType, data) ->
            entity.facets.getOrPut(facetType) { mutableMapOf() }["global"] = data
        }
        return entity
    }
}
