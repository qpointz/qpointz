package io.qpointz.mill.metadata.api;

import io.qpointz.mill.metadata.api.dto.FacetDto;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for scope-aware facet read operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/metadata/v1/facets")
@ConditionalOnBean(MetadataService.class)
@RequiredArgsConstructor
@Tag(name = "Facets", description = "Scope-aware facet management endpoints")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:8080"})
public class FacetController {
    
    private final MetadataService metadataService;
    
    /**
     * Get merged facet for current user (global + user + team + role).
     */
    @Operation(
        summary = "Get merged facet",
        description = "Retrieves a facet merged from multiple scopes (user > team > role > global) for the specified user context",
        parameters = {
            @Parameter(name = "entityId", description = "Entity ID", required = true, example = "moneta.clients"),
            @Parameter(name = "facetType", description = "Facet type", required = true, example = "descriptive"),
            @Parameter(name = "userId", description = "User ID for user-scoped facets", required = false, example = "alice@company.com"),
            @Parameter(name = "teams", description = "List of team names for team-scoped facets", required = false, example = "engineering"),
            @Parameter(name = "roles", description = "List of role names for role-scoped facets", required = false, example = "admin")
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Merged facet found",
            content = @Content(schema = @Schema(implementation = FacetDto.class))),
        @ApiResponse(responseCode = "404", description = "Entity or facet not found")
    })
    @GetMapping("/entities/{entityId}/types/{facetType}")
    public ResponseEntity<FacetDto> getMergedFacet(
        @PathVariable String entityId,
        @PathVariable String facetType,
        @RequestParam(name = "userId", required = false) String userId,
        @RequestParam(name = "teams", required = false) List<String> teams,
        @RequestParam(name = "roles", required = false) List<String> roles
    ) {
        return metadataService.findById(entityId)
            .map(entity -> {
                // Get merged facet
                Optional<Object> mergedData = entity.getMergedFacet(
                    facetType,
                    userId != null ? userId : "anonymous",
                    teams != null ? teams : List.of(),
                    roles != null ? roles : List.of(),
                    Object.class
                );
                
                Set<String> scopes = entity.getFacetScopes(facetType);
                
                return mergedData.map(data -> FacetDto.builder()
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
     * Get facet for specific scope.
     */
    @Operation(
        summary = "Get facet by scope",
        description = "Retrieves a facet for a specific scope (e.g., global, user:alice@company.com, team:engineering)",
        parameters = {
            @Parameter(name = "entityId", description = "Entity ID", required = true, example = "moneta.clients"),
            @Parameter(name = "facetType", description = "Facet type", required = true, example = "descriptive"),
            @Parameter(name = "scope", description = "Scope (e.g., global, user:alice@company.com)", required = true, example = "global")
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Facet found",
            content = @Content(schema = @Schema(implementation = FacetDto.class))),
        @ApiResponse(responseCode = "404", description = "Entity or facet not found")
    })
    @GetMapping("/entities/{entityId}/types/{facetType}/scopes/{scope}")
    public ResponseEntity<FacetDto> getFacetByScope(
        @PathVariable String entityId,
        @PathVariable String facetType,
        @PathVariable String scope
    ) {
        return metadataService.findById(entityId)
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
    
}

