package io.qpointz.mill.metadata.api;

import io.qpointz.mill.metadata.api.dto.SearchResultDto;
import io.qpointz.mill.metadata.api.dto.TreeNodeDto;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for schema exploration and search.
 */
@Slf4j
@RestController
@RequestMapping("/api/metadata/v1/explorer")
@ConditionalOnBean(MetadataService.class)
@RequiredArgsConstructor
@Tag(name = "Schema Explorer", description = "Schema exploration, search, and navigation endpoints")
public class SchemaExplorerController {
    
    private final MetadataService metadataService;
    private final DtoMapper dtoMapper;
    
    /**
     * Get schema tree structure.
     */
    @Operation(
        summary = "Get schema tree",
        description = "Retrieves a hierarchical tree structure of schemas and tables, optionally filtered by schema",
        parameters = {
            @Parameter(name = "schema", description = "Filter by schema name", required = false, example = "moneta"),
            @Parameter(name = "scope", description = "Scope for facet merging (default: global)", required = false, example = "global")
        }
    )
    @ApiResponse(responseCode = "200", description = "Tree structure",
        content = @Content(schema = @Schema(implementation = TreeNodeDto.class)))
    @GetMapping("/tree")
    public ResponseEntity<List<TreeNodeDto>> getTree(
        @RequestParam(name = "schema", required = false) String schema,
        @RequestParam(name = "scope", required = false, defaultValue = "global") String scope
    ) {
        List<MetadataEntity> entities = metadataService.findAll();
        
        // Filter by schema if provided
        List<MetadataEntity> filtered = entities.stream()
            .filter(e -> schema == null || Objects.equals(e.getSchemaName(), schema))
            .toList();
        
        // Build tree structure
        List<TreeNodeDto> tree = buildTree(filtered, scope);
        return ResponseEntity.ok(tree);
    }
    
    /**
     * Search metadata entities.
     */
    @Operation(
        summary = "Search metadata",
        description = "Searches metadata entities by query string, optionally filtered by type",
        parameters = {
            @Parameter(name = "q", description = "Search query string", required = true, example = "client"),
            @Parameter(name = "type", description = "Filter by metadata type (e.g., TABLE, ATTRIBUTE)", required = false, example = "TABLE"),
            @Parameter(name = "scope", description = "Scope for facet merging (default: global)", required = false, example = "global")
        }
    )
    @ApiResponse(responseCode = "200", description = "Search results",
        content = @Content(schema = @Schema(implementation = SearchResultDto.class)))
    @GetMapping("/search")
    public ResponseEntity<List<SearchResultDto>> search(
        @RequestParam(name = "q") String q,
        @RequestParam(name = "type", required = false) String type,
        @RequestParam(name = "scope", required = false, defaultValue = "global") String scope
    ) {
        List<MetadataEntity> entities = metadataService.findAll();
        
        // Simple text search (case-insensitive)
        String query = q.toLowerCase();
        List<SearchResultDto> results = entities.stream()
            .filter(e -> type == null || e.getType().name().equalsIgnoreCase(type))
            .filter(e -> matchesQuery(e, query))
            .map(e -> toSearchResult(e, scope))
            .sorted(Comparator.comparing(SearchResultDto::getName))
            .toList();
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Get lineage for a table.
     */
    @Operation(
        summary = "Get data lineage",
        description = "Retrieves data lineage information for a table, showing upstream and downstream dependencies",
        parameters = {
            @Parameter(name = "table", description = "Table fully qualified name", required = true, example = "moneta.clients"),
            @Parameter(name = "depth", description = "Lineage traversal depth (default: 1)", required = false, example = "1")
        }
    )
    @ApiResponse(responseCode = "200", description = "Lineage information")
    @GetMapping("/lineage")
    public ResponseEntity<Map<String, Object>> getLineage(
        @RequestParam(name = "table") String table,
        @RequestParam(name = "depth", required = false, defaultValue = "1") int depth
    ) {
        // TODO: Implement lineage traversal
        // For now, return empty structure
        Map<String, Object> lineage = Map.of(
            "table", table,
            "depth", depth,
            "upstream", List.of(),
            "downstream", List.of()
        );
        return ResponseEntity.ok(lineage);
    }
    
    private List<TreeNodeDto> buildTree(List<MetadataEntity> entities, String scope) {
        // Group by schema
        Map<String, List<MetadataEntity>> bySchema = entities.stream()
            .filter(e -> e.getSchemaName() != null)
            .collect(Collectors.groupingBy(MetadataEntity::getSchemaName));
        
        return bySchema.entrySet().stream()
            .map(entry -> {
                String schemaName = entry.getKey();
                List<MetadataEntity> schemaEntities = entry.getValue();
                
                // Get tables in this schema
                List<MetadataEntity> tableEntities = schemaEntities.stream()
                    .filter(e -> e.getType() == MetadataType.TABLE)
                    .sorted(Comparator.comparing(MetadataEntity::getTableName))
                    .toList();
                
                // Build table nodes with their attributes
                List<TreeNodeDto> tables = tableEntities.stream()
                    .map(tableEntity -> {
                        // Get attributes for this table
                        List<MetadataEntity> attributes = schemaEntities.stream()
                            .filter(e -> e.getType() == MetadataType.ATTRIBUTE)
                            .filter(e -> Objects.equals(e.getTableName(), tableEntity.getTableName()))
                            .sorted(Comparator.comparing(MetadataEntity::getAttributeName))
                            .toList();
                        
                        // Build attribute tree nodes
                        List<TreeNodeDto> attributeNodes = attributes.stream()
                            .map(attr -> toTreeNode(attr, scope, false))
                            .toList();
                        
                        // Build table node with attributes as children
                        TreeNodeDto tableNode = toTreeNode(tableEntity, scope, true);
                        tableNode.setChildren(attributeNodes);
                        tableNode.setHasChildren(!attributeNodes.isEmpty());
                        return tableNode;
                    })
                    .sorted(Comparator.comparing(TreeNodeDto::getName))
                    .toList();
                
                return TreeNodeDto.builder()
                    .id(schemaName)
                    .name(schemaName)
                    .type(MetadataType.SCHEMA)
                    .displayName(schemaName)
                    .children(tables)
                    .hasChildren(!tables.isEmpty())
                    .build();
            })
            .sorted(Comparator.comparing(TreeNodeDto::getName))
            .toList();
    }
    
    private TreeNodeDto toTreeNode(MetadataEntity entity, String scope, boolean includeChildren) {
        String displayName = entity.getFacet("descriptive", scope, Map.class)
            .map(f -> (String) ((Map<?, ?>) f).get("displayName"))
            .orElse(getEntityName(entity));
        
        String description = entity.getFacet("descriptive", scope, Map.class)
            .map(f -> (String) ((Map<?, ?>) f).get("description"))
            .orElse(null);
        
        String name = getEntityName(entity);
        
        return TreeNodeDto.builder()
            .id(entity.getId())
            .name(name)
            .type(entity.getType())
            .displayName(displayName)
            .description(description)
            .children(includeChildren ? List.of() : null)
            .hasChildren(false)
            .build();
    }
    
    private String getEntityName(MetadataEntity entity) {
        if (entity.getAttributeName() != null) {
            return entity.getAttributeName();
        }
        if (entity.getTableName() != null) {
            return entity.getTableName();
        }
        if (entity.getSchemaName() != null) {
            return entity.getSchemaName();
        }
        return entity.getId();
    }
    
    private boolean matchesQuery(MetadataEntity entity, String query) {
        // Search in ID, name, description
        if (entity.getId() != null && entity.getId().toLowerCase().contains(query)) {
            return true;
        }
        if (entity.getTableName() != null && entity.getTableName().toLowerCase().contains(query)) {
            return true;
        }
        if (entity.getAttributeName() != null && entity.getAttributeName().toLowerCase().contains(query)) {
            return true;
        }
        // Search in descriptive facet
        Optional<Map> descriptive = entity.getFacet("descriptive", "global", Map.class);
        if (descriptive.isPresent()) {
            Map<?, ?> desc = descriptive.get();
            if (desc.get("displayName") != null && 
                desc.get("displayName").toString().toLowerCase().contains(query)) {
                return true;
            }
            if (desc.get("description") != null && 
                desc.get("description").toString().toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }
    
    private SearchResultDto toSearchResult(MetadataEntity entity, String scope) {
        String displayName = entity.getFacet("descriptive", scope, Map.class)
            .map(f -> (String) ((Map<?, ?>) f).get("displayName"))
            .orElse(entity.getTableName() != null ? entity.getTableName() : entity.getId());
        
        String description = entity.getFacet("descriptive", scope, Map.class)
            .map(f -> (String) ((Map<?, ?>) f).get("description"))
            .orElse(null);
        
        String location = buildLocation(entity);
        
        return SearchResultDto.builder()
            .id(entity.getId())
            .name(entity.getTableName() != null ? entity.getTableName() : entity.getId())
            .type(entity.getType())
            .displayName(displayName)
            .description(description)
            .location(location)
            .build();
    }
    
    private String buildLocation(MetadataEntity entity) {
        if (entity.getAttributeName() != null) {
            return String.format("%s.%s.%s.%s", 
                entity.getSchemaName(), entity.getTableName(), entity.getAttributeName());
        }
        if (entity.getTableName() != null) {
            return String.format("%s.%s", entity.getSchemaName(), entity.getTableName());
        }
        if (entity.getSchemaName() != null) {
            return entity.getSchemaName();
        }
        return entity.getId();
    }
}

