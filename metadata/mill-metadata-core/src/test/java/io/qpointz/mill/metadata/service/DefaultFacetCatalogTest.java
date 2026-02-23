package io.qpointz.mill.metadata.service;

import io.qpointz.mill.metadata.domain.*;
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFacetCatalogTest {

    private InMemoryFacetTypeRepository repository;
    private DefaultFacetCatalog catalog;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFacetTypeRepository();
        catalog = new DefaultFacetCatalog(repository);
    }

    private FacetTypeDescriptor mandatoryDescriptor(String typeKey, MetadataTargetType... targets) {
        return FacetTypeDescriptor.builder()
                .typeKey(typeKey)
                .mandatory(true)
                .enabled(true)
                .displayName(typeKey)
                .applicableTo(Set.of(targets))
                .version("1.0")
                .build();
    }

    private FacetTypeDescriptor optionalDescriptor(String typeKey, MetadataTargetType... targets) {
        return FacetTypeDescriptor.builder()
                .typeKey(typeKey)
                .mandatory(false)
                .enabled(true)
                .displayName(typeKey)
                .applicableTo(targets.length > 0 ? Set.of(targets) : null)
                .version("1.0")
                .build();
    }

    @Test
    void shouldRegister_newFacetType() {
        catalog.register(optionalDescriptor("custom"));
        assertTrue(catalog.get("custom").isPresent());
    }

    @Test
    void shouldReject_duplicateRegistration() {
        catalog.register(optionalDescriptor("custom"));
        assertThrows(IllegalArgumentException.class, () ->
                catalog.register(optionalDescriptor("custom")));
    }

    @Test
    void shouldUpdate_existingFacetType() {
        catalog.register(optionalDescriptor("custom"));
        var updated = optionalDescriptor("custom");
        updated.setDisplayName("Updated Custom");
        catalog.update(updated);
        assertEquals("Updated Custom", catalog.get("custom").orElseThrow().getDisplayName());
    }

    @Test
    void shouldReject_updateOfUnknownType() {
        assertThrows(IllegalArgumentException.class, () ->
                catalog.update(optionalDescriptor("nonexistent")));
    }

    @Test
    void shouldReject_disablingMandatoryType() {
        catalog.register(mandatoryDescriptor("structural", MetadataTargetType.TABLE));
        var disabled = mandatoryDescriptor("structural", MetadataTargetType.TABLE);
        disabled.setEnabled(false);
        assertThrows(IllegalArgumentException.class, () -> catalog.update(disabled));
    }

    @Test
    void shouldDelete_optionalType() {
        catalog.register(optionalDescriptor("custom"));
        catalog.delete("custom");
        assertTrue(catalog.get("custom").isEmpty());
    }

    @Test
    void shouldReject_deletionOfMandatoryType() {
        catalog.register(mandatoryDescriptor("structural", MetadataTargetType.TABLE));
        assertThrows(IllegalArgumentException.class, () -> catalog.delete("structural"));
    }

    @Test
    void shouldReturnAll_enabledTypes() {
        catalog.register(optionalDescriptor("enabled-one"));
        var disabled = optionalDescriptor("disabled-one");
        disabled.setEnabled(false);
        catalog.register(disabled);

        assertEquals(1, catalog.getEnabled().size());
    }

    @Test
    void shouldReturnAll_mandatoryTypes() {
        catalog.register(mandatoryDescriptor("m1", MetadataTargetType.TABLE));
        catalog.register(optionalDescriptor("o1"));

        assertEquals(1, catalog.getMandatory().size());
    }

    @Test
    void shouldFilter_byTargetType() {
        catalog.register(mandatoryDescriptor("structural", MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE));
        catalog.register(mandatoryDescriptor("relation", MetadataTargetType.TABLE));
        catalog.register(optionalDescriptor("concept", MetadataTargetType.CONCEPT));

        var forTable = catalog.getForTargetType(MetadataTargetType.TABLE);
        assertEquals(2, forTable.size());

        var forConcept = catalog.getForTargetType(MetadataTargetType.CONCEPT);
        assertEquals(1, forConcept.size());
    }

    @Test
    void shouldAllowUnknownTypeKeys() {
        assertTrue(catalog.isAllowed("unknown-type"));
    }

    @Test
    void shouldCheckApplicability() {
        catalog.register(mandatoryDescriptor("structural", MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE));

        assertTrue(catalog.isApplicableTo("structural", MetadataTargetType.TABLE));
        assertFalse(catalog.isApplicableTo("structural", MetadataTargetType.CONCEPT));
        assertTrue(catalog.isApplicableTo("unknown", MetadataTargetType.TABLE));
    }

    @Test
    void shouldValidateEntityFacets_targetTypeMismatch() {
        catalog.register(mandatoryDescriptor("structural", MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE));

        var entity = new MetadataEntity();
        entity.setId("test");
        entity.setType(MetadataType.CONCEPT);
        entity.setFacet("structural", "global", Map.of("physicalName", "TEST"));

        ValidationResult result = catalog.validateEntityFacets(entity);
        assertFalse(result.valid());
        assertTrue(result.errors().get(0).contains("not applicable"));
    }

    @Test
    void shouldPass_validEntityFacets() {
        catalog.register(mandatoryDescriptor("structural", MetadataTargetType.TABLE));

        var entity = new MetadataEntity();
        entity.setId("test");
        entity.setType(MetadataType.TABLE);
        entity.setFacet("structural", "global", Map.of("physicalName", "TEST"));

        ValidationResult result = catalog.validateEntityFacets(entity);
        assertTrue(result.valid());
    }

    @Test
    void shouldSkipValidation_forUnknownFacetTypes() {
        var entity = new MetadataEntity();
        entity.setId("test");
        entity.setType(MetadataType.TABLE);
        entity.setFacet("custom-unknown", "global", Map.of("key", "value"));

        ValidationResult result = catalog.validateEntityFacets(entity);
        assertTrue(result.valid());
    }

    @Test
    void shouldReject_disabledFacetType() {
        var disabled = optionalDescriptor("disabled-facet");
        disabled.setEnabled(false);
        catalog.register(disabled);

        var entity = new MetadataEntity();
        entity.setId("test");
        entity.setType(MetadataType.TABLE);
        entity.setFacet("disabled-facet", "global", Map.of("key", "value"));

        ValidationResult result = catalog.validateEntityFacets(entity);
        assertFalse(result.valid());
        assertTrue(result.errors().get(0).contains("disabled"));
    }
}
