package io.qpointz.mill.metadata.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FacetTypeDescriptorTest {

    @Test
    void shouldBeApplicableToAny_whenApplicableToIsNull() {
        var descriptor = FacetTypeDescriptor.builder()
                .typeKey("test")
                .applicableTo(null)
                .build();

        assertTrue(descriptor.isApplicableTo(MetadataTargetType.TABLE));
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.CONCEPT));
    }

    @Test
    void shouldBeApplicableToAny_whenApplicableToIsEmpty() {
        var descriptor = FacetTypeDescriptor.builder()
                .typeKey("test")
                .applicableTo(Set.of())
                .build();

        assertTrue(descriptor.isApplicableTo(MetadataTargetType.TABLE));
    }

    @Test
    void shouldBeApplicableToAny_whenContainsANY() {
        var descriptor = FacetTypeDescriptor.builder()
                .typeKey("test")
                .applicableTo(Set.of(MetadataTargetType.ANY))
                .build();

        assertTrue(descriptor.isApplicableTo(MetadataTargetType.TABLE));
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.CONCEPT));
    }

    @Test
    void shouldRestrict_whenApplicableToIsSpecific() {
        var descriptor = FacetTypeDescriptor.builder()
                .typeKey("structural")
                .applicableTo(Set.of(MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE))
                .build();

        assertTrue(descriptor.isApplicableTo(MetadataTargetType.TABLE));
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.ATTRIBUTE));
        assertFalse(descriptor.isApplicableTo(MetadataTargetType.CONCEPT));
        assertFalse(descriptor.isApplicableTo(MetadataTargetType.SCHEMA));
    }

    @Test
    void shouldDetectContentSchema() {
        var withSchema = FacetTypeDescriptor.builder()
                .typeKey("test")
                .contentSchema(java.util.Map.of("type", "object"))
                .build();
        assertTrue(withSchema.hasContentSchema());

        var withoutSchema = FacetTypeDescriptor.builder()
                .typeKey("test")
                .build();
        assertFalse(withoutSchema.hasContentSchema());

        var emptySchema = FacetTypeDescriptor.builder()
                .typeKey("test")
                .contentSchema(java.util.Map.of())
                .build();
        assertFalse(emptySchema.hasContentSchema());
    }

    @Test
    void shouldHaveCorrectDefaults() {
        var descriptor = FacetTypeDescriptor.builder()
                .typeKey("test")
                .build();

        assertFalse(descriptor.isMandatory());
        assertTrue(descriptor.isEnabled());
    }
}
