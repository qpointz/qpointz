package io.qpointz.mill.metadata.api;

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor;
import io.qpointz.mill.metadata.domain.MetadataTargetType;
import io.qpointz.mill.metadata.service.FacetCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/metadata/v1/facet-types")
@ConditionalOnBean(FacetCatalog.class)
@RequiredArgsConstructor
@Tag(name = "Facet Types", description = "Facet type catalog management")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
public class FacetTypeController {

    private final FacetCatalog facetCatalog;

    @Operation(summary = "List all registered facet types")
    @GetMapping
    public Collection<FacetTypeDescriptor> listAll(
            @RequestParam(name = "targetType", required = false) MetadataTargetType targetType,
            @RequestParam(name = "enabledOnly", required = false, defaultValue = "false") boolean enabledOnly) {
        if (targetType != null) {
            return facetCatalog.getForTargetType(targetType);
        }
        return enabledOnly ? facetCatalog.getEnabled() : facetCatalog.getAll();
    }

    @Operation(summary = "Get facet type descriptor by type key")
    @GetMapping("/{typeKey}")
    public ResponseEntity<FacetTypeDescriptor> getByTypeKey(@PathVariable String typeKey) {
        return facetCatalog.get(typeKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Register a new custom facet type")
    @PostMapping
    public ResponseEntity<FacetTypeDescriptor> create(@RequestBody FacetTypeDescriptor descriptor) {
        descriptor.setCreatedAt(Instant.now());
        descriptor.setUpdatedAt(Instant.now());
        try {
            facetCatalog.register(descriptor);
            return ResponseEntity.ok(descriptor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update an existing facet type (non-mandatory only)")
    @PutMapping("/{typeKey}")
    public ResponseEntity<FacetTypeDescriptor> update(@PathVariable String typeKey,
                                                       @RequestBody FacetTypeDescriptor descriptor) {
        descriptor.setTypeKey(typeKey);
        descriptor.setUpdatedAt(Instant.now());
        try {
            facetCatalog.update(descriptor);
            return ResponseEntity.ok(descriptor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete a custom facet type (non-mandatory only)")
    @DeleteMapping("/{typeKey}")
    public ResponseEntity<Void> delete(@PathVariable String typeKey) {
        try {
            facetCatalog.delete(typeKey);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
