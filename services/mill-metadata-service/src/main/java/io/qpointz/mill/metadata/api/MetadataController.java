package io.qpointz.mill.metadata.api;

import io.qpointz.mill.metadata.api.dto.FacetDto;
import io.qpointz.mill.metadata.api.dto.MetadataEntityDto;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * REST controller for metadata entity read operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/metadata/v1")
@RequiredArgsConstructor
@Tag(name = "Metadata", description = "Metadata entity CRUD operations")
public class MetadataController {
    
    private final MetadataService metadataService;
    private final DtoMapper dtoMapper;
    
    /**
     * Get entity by ID.
     */
    @Operation(
        summary = "Get entity by ID",
        description = "Retrieves a metadata entity by its unique identifier with optional scope filtering",
        parameters = {
            @Parameter(name = "id", description = "Entity ID", required = true, example = "moneta.clients"),
            @Parameter(name = "scope", description = "Scope for facet merging (default: global)", required = false, example = "global")
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entity found", 
            content = @Content(schema = @Schema(implementation = MetadataEntityDto.class))),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/entities/{id}")
    public ResponseEntity<MetadataEntityDto> getEntityById(
        @PathVariable String id,
        @RequestParam(required = false, defaultValue = "global") String scope
    ) {
        return metadataService.findById(id)
            .map(entity -> dtoMapper.toDto(entity, scope))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get entity by location.
     */
    @Operation(
        summary = "Get table by location",
        description = "Retrieves a table metadata entity by schema and table name",
        parameters = {
            @Parameter(name = "schema", description = "Schema name", required = true, example = "moneta"),
            @Parameter(name = "table", description = "Table name", required = true, example = "clients"),
            @Parameter(name = "scope", description = "Scope for facet merging (default: global)", required = false, example = "global")
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Table found",
            content = @Content(schema = @Schema(implementation = MetadataEntityDto.class))),
        @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/schemas/{schema}/tables/{table}")
    public ResponseEntity<MetadataEntityDto> getTable(
        @PathVariable String schema,
        @PathVariable String table,
        @RequestParam(required = false, defaultValue = "global") String scope
    ) {
        return metadataService.findByLocation(schema, table, null)
            .map(entity -> dtoMapper.toDto(entity, scope))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get attribute by location.
     */
    @Operation(
        summary = "Get attribute by location",
        description = "Retrieves an attribute (column) metadata entity by its hierarchical location",
        parameters = {
            @Parameter(name = "schema", description = "Schema name", required = true, example = "moneta"),
            @Parameter(name = "table", description = "Table name", required = true, example = "clients"),
            @Parameter(name = "attribute", description = "Attribute name", required = true, example = "id"),
            @Parameter(name = "scope", description = "Scope for facet merging (default: global)", required = false, example = "global")
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attribute found",
            content = @Content(schema = @Schema(implementation = MetadataEntityDto.class))),
        @ApiResponse(responseCode = "404", description = "Attribute not found")
    })
    @GetMapping("/schemas/{schema}/tables/{table}/attributes/{attribute}")
    public ResponseEntity<MetadataEntityDto> getAttribute(
        @PathVariable String schema,
        @PathVariable String table,
        @PathVariable String attribute,
        @RequestParam(required = false, defaultValue = "global") String scope
    ) {
        return metadataService.findByLocation(schema, table, attribute)
            .map(entity -> dtoMapper.toDto(entity, scope))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all entities of a type.
     */
    @Operation(
        summary = "List all entities",
        description = "Retrieves all metadata entities, optionally filtered by type",
        parameters = {
            @Parameter(name = "type", description = "Filter by metadata type (e.g., TABLE, ATTRIBUTE)", required = false, example = "TABLE"),
            @Parameter(name = "scope", description = "Scope for facet merging (default: global)", required = false, example = "global")
        }
    )
    @ApiResponse(responseCode = "200", description = "List of entities",
        content = @Content(schema = @Schema(implementation = MetadataEntityDto.class)))
    @GetMapping("/entities")
    public ResponseEntity<List<MetadataEntityDto>> getEntities(
        @RequestParam(required = false) String type,
        @RequestParam(required = false, defaultValue = "global") String scope
    ) {
        // For now, return all entities. In future, filter by type if provided.
        List<MetadataEntityDto> entities = metadataService.findAll().stream()
            .map(entity -> dtoMapper.toDto(entity, scope))
            .toList();
        return ResponseEntity.ok(entities);
    }
    
    /**
     * Get facet for entity.
     */
    @Operation(
        summary = "Get facet by type",
        description = "Retrieves a specific facet for an entity by facet type and scope",
        parameters = {
            @Parameter(name = "id", description = "Entity ID", required = true, example = "moneta.clients"),
            @Parameter(name = "facetType", description = "Facet type (e.g., descriptive, structural, value-mapping)", required = true, example = "descriptive"),
            @Parameter(name = "scope", description = "Scope for facet (default: global)", required = false, example = "global")
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Facet found",
            content = @Content(schema = @Schema(implementation = FacetDto.class))),
        @ApiResponse(responseCode = "404", description = "Entity or facet not found")
    })
    @GetMapping("/entities/{id}/facets/{facetType}")
    public ResponseEntity<FacetDto> getFacet(
        @PathVariable String id,
        @PathVariable String facetType,
        @RequestParam(required = false, defaultValue = "global") String scope
    ) {
        return metadataService.findById(id)
            .map(entity -> {
                Optional<Object> facetData = entity.getFacet(facetType, scope, Object.class);
                Set<String> scopes = entity.getFacetScopes(facetType);
                
                return facetData.map(data -> FacetDto.builder()
                    .facetType(facetType)
                    .data(data)
                    .availableScopes(scopes)
                    .build());
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all scopes for a facet.
     */
    @Operation(
        summary = "Get facet scopes",
        description = "Retrieves all available scopes for a specific facet type on an entity",
        parameters = {
            @Parameter(name = "id", description = "Entity ID", required = true, example = "moneta.clients"),
            @Parameter(name = "facetType", description = "Facet type", required = true, example = "descriptive")
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of scopes"),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/entities/{id}/facets/{facetType}/scopes")
    public ResponseEntity<Set<String>> getFacetScopes(
        @PathVariable String id,
        @PathVariable String facetType
    ) {
        return metadataService.findById(id)
            .map(entity -> entity.getFacetScopes(facetType))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get related entities.
     */
    @Operation(
        summary = "Get related entities",
        description = "Retrieves all entities related to the given entity (bidirectional). " +
                     "Finds entities that reference this entity and entities that this entity references.",
        parameters = {
            @Parameter(name = "id", description = "Entity ID", required = true, example = "moneta.clients"),
            @Parameter(name = "scope", description = "Scope for facet access (default: global)", required = false, example = "global")
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of related entities",
            content = @Content(schema = @Schema(implementation = MetadataEntityDto.class))),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/entities/{id}/related")
    public ResponseEntity<List<MetadataEntityDto>> getRelatedEntities(
        @PathVariable String id,
        @RequestParam(required = false, defaultValue = "global") String scope
    ) {
        return metadataService.findById(id)
            .map(entity -> {
                List<MetadataEntity> relatedEntities = metadataService.findRelatedEntities(id, scope);
                List<MetadataEntityDto> relatedDtos = relatedEntities.stream()
                    .map(e -> dtoMapper.toDto(e, scope))
                    .toList();
                return ResponseEntity.ok(relatedDtos);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
}

