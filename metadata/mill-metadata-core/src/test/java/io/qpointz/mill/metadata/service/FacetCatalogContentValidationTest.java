package io.qpointz.mill.metadata.service;

import io.qpointz.mill.metadata.domain.*;
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FacetCatalogContentValidationTest {

    private InMemoryFacetTypeRepository repository;
    private DefaultFacetCatalog catalog;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFacetTypeRepository();
        catalog = new DefaultFacetCatalog(repository, new JsonSchemaFacetContentValidator());

        repository.save(FacetTypeDescriptor.builder()
                .typeKey("audit")
                .mandatory(false)
                .enabled(true)
                .displayName("Audit")
                .applicableTo(Set.of(MetadataTargetType.TABLE))
                .version("1.0")
                .contentSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "auditor", Map.of("type", "string"),
                                "status", Map.of("type", "string",
                                        "enum", List.of("passed", "failed", "pending"))
                        ),
                        "required", List.of("status")
                ))
                .build());
    }

    @Test
    void shouldPass_validContent() {
        ValidationResult result = catalog.validateFacetContent("audit",
                Map.of("auditor", "alice", "status", "passed"));
        assertTrue(result.valid());
    }

    @Test
    void shouldFail_missingRequired() {
        ValidationResult result = catalog.validateFacetContent("audit",
                Map.of("auditor", "alice"));
        assertFalse(result.valid());
    }

    @Test
    void shouldFail_invalidEnum() {
        ValidationResult result = catalog.validateFacetContent("audit",
                Map.of("status", "invalid-value"));
        assertFalse(result.valid());
    }

    @Test
    void shouldPass_unknownTypeKey() {
        ValidationResult result = catalog.validateFacetContent("unknown",
                Map.of("anything", "goes"));
        assertTrue(result.valid());
    }

    @Test
    void shouldValidateEntityFacets_withContentSchema() {
        var entity = new MetadataEntity();
        entity.setId("test");
        entity.setType(MetadataType.TABLE);
        entity.setFacet("audit", "global", Map.of("status", "invalid-value"));

        ValidationResult result = catalog.validateEntityFacets(entity);
        assertFalse(result.valid());
    }

    @Test
    void shouldPass_entityFacets_withValidContent() {
        var entity = new MetadataEntity();
        entity.setId("test");
        entity.setType(MetadataType.TABLE);
        entity.setFacet("audit", "global", Map.of("status", "passed", "auditor", "bob"));

        ValidationResult result = catalog.validateEntityFacets(entity);
        assertTrue(result.valid());
    }
}
