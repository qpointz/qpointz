package io.qpointz.mill.data.backend.access.http.explorer;

import io.qpointz.mill.data.backend.access.http.explorer.dto.SearchResultDto;
import io.qpointz.mill.data.backend.access.http.explorer.dto.TreeNodeDto;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/metadata/v1/explorer")
@ConditionalOnBean(MetadataService.class)
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
public class SchemaExplorerController {

    private final MetadataService metadataService;

    @GetMapping("/tree")
    public ResponseEntity<List<TreeNodeDto>> getTree(
            @RequestParam(name = "schema", required = false) String schema,
            @RequestParam(name = "scope", required = false, defaultValue = "global") String scope) {
        List<MetadataEntity> entities = metadataService.findAll();
        List<MetadataEntity> filtered = entities.stream()
                .filter(e -> schema == null || Objects.equals(e.getSchemaName(), schema))
                .toList();
        return ResponseEntity.ok(buildTree(filtered, scope));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchResultDto>> search(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "scope", required = false, defaultValue = "global") String scope) {
        String query = q.toLowerCase();
        List<SearchResultDto> results = metadataService.findAll().stream()
                .filter(e -> type == null || e.getType().name().equalsIgnoreCase(type))
                .filter(e -> matchesQuery(e, query))
                .map(e -> toSearchResult(e, scope))
                .sorted(Comparator.comparing(SearchResultDto::getName))
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/lineage")
    public ResponseEntity<Map<String, Object>> getLineage(
            @RequestParam(name = "table") String table,
            @RequestParam(name = "depth", required = false, defaultValue = "1") int depth) {
        Map<String, Object> lineage = Map.of(
                "table", table,
                "depth", depth,
                "upstream", List.of(),
                "downstream", List.of());
        return ResponseEntity.ok(lineage);
    }

    private List<TreeNodeDto> buildTree(List<MetadataEntity> entities, String scope) {
        Map<String, List<MetadataEntity>> bySchema = entities.stream()
                .filter(e -> e.getSchemaName() != null)
                .collect(Collectors.groupingBy(MetadataEntity::getSchemaName));

        return bySchema.entrySet().stream()
                .map(entry -> {
                    String schemaName = entry.getKey();
                    List<MetadataEntity> schemaEntities = entry.getValue();

                    List<MetadataEntity> tableEntities = schemaEntities.stream()
                            .filter(e -> e.getType() == MetadataType.TABLE)
                            .sorted(Comparator.comparing(MetadataEntity::getTableName))
                            .toList();

                    List<TreeNodeDto> tables = tableEntities.stream()
                            .map(tableEntity -> {
                                List<TreeNodeDto> attributeNodes = schemaEntities.stream()
                                        .filter(e -> e.getType() == MetadataType.ATTRIBUTE)
                                        .filter(e -> Objects.equals(e.getTableName(), tableEntity.getTableName()))
                                        .sorted(Comparator.comparing(MetadataEntity::getAttributeName))
                                        .map(attr -> toTreeNode(attr, scope, false))
                                        .toList();

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

    @SuppressWarnings("unchecked")
    private TreeNodeDto toTreeNode(MetadataEntity entity, String scope, boolean includeChildren) {
        String displayName = entity.getFacet("descriptive", scope, Map.class)
                .map(f -> (String) ((Map<?, ?>) f).get("displayName"))
                .orElse(getEntityName(entity));
        String description = entity.getFacet("descriptive", scope, Map.class)
                .map(f -> (String) ((Map<?, ?>) f).get("description"))
                .orElse(null);

        return TreeNodeDto.builder()
                .id(entity.getId())
                .name(getEntityName(entity))
                .type(entity.getType())
                .displayName(displayName)
                .description(description)
                .children(includeChildren ? List.of() : null)
                .hasChildren(false)
                .build();
    }

    private String getEntityName(MetadataEntity entity) {
        if (entity.getAttributeName() != null) return entity.getAttributeName();
        if (entity.getTableName() != null) return entity.getTableName();
        if (entity.getSchemaName() != null) return entity.getSchemaName();
        return entity.getId();
    }

    @SuppressWarnings("unchecked")
    private boolean matchesQuery(MetadataEntity entity, String query) {
        if (entity.getId() != null && entity.getId().toLowerCase().contains(query)) return true;
        if (entity.getTableName() != null && entity.getTableName().toLowerCase().contains(query)) return true;
        if (entity.getAttributeName() != null && entity.getAttributeName().toLowerCase().contains(query)) return true;
        Optional<Map> descriptive = entity.getFacet("descriptive", "global", Map.class);
        if (descriptive.isPresent()) {
            Map<?, ?> desc = descriptive.get();
            if (desc.get("displayName") != null && desc.get("displayName").toString().toLowerCase().contains(query))
                return true;
            if (desc.get("description") != null && desc.get("description").toString().toLowerCase().contains(query))
                return true;
        }
        return false;
    }

    private SearchResultDto toSearchResult(MetadataEntity entity, String scope) {
        @SuppressWarnings("unchecked")
        String displayName = entity.getFacet("descriptive", scope, Map.class)
                .map(f -> (String) ((Map<?, ?>) f).get("displayName"))
                .orElse(entity.getTableName() != null ? entity.getTableName() : entity.getId());
        @SuppressWarnings("unchecked")
        String description = entity.getFacet("descriptive", scope, Map.class)
                .map(f -> (String) ((Map<?, ?>) f).get("description"))
                .orElse(null);

        return SearchResultDto.builder()
                .id(entity.getId())
                .name(entity.getTableName() != null ? entity.getTableName() : entity.getId())
                .type(entity.getType())
                .displayName(displayName)
                .description(description)
                .location(buildLocation(entity))
                .build();
    }

    private String buildLocation(MetadataEntity entity) {
        if (entity.getAttributeName() != null) {
            return String.format("%s.%s.%s", entity.getSchemaName(), entity.getTableName(), entity.getAttributeName());
        }
        if (entity.getTableName() != null) {
            return String.format("%s.%s", entity.getSchemaName(), entity.getTableName());
        }
        if (entity.getSchemaName() != null) return entity.getSchemaName();
        return entity.getId();
    }
}
