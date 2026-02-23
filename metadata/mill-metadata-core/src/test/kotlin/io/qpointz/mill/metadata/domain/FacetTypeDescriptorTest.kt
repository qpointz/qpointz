package io.qpointz.mill.metadata.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FacetTypeDescriptorTest {

    @Test
    fun shouldBeApplicableToAny_whenApplicableToIsNull() {
        val descriptor = FacetTypeDescriptor(typeKey = "test", applicableTo = null)
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.TABLE))
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.CONCEPT))
    }

    @Test
    fun shouldBeApplicableToAny_whenApplicableToIsEmpty() {
        val descriptor = FacetTypeDescriptor(typeKey = "test", applicableTo = emptySet())
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.TABLE))
    }

    @Test
    fun shouldBeApplicableToAny_whenContainsANY() {
        val descriptor = FacetTypeDescriptor(typeKey = "test", applicableTo = setOf(MetadataTargetType.ANY))
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.TABLE))
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.CONCEPT))
    }

    @Test
    fun shouldRestrict_whenApplicableToIsSpecific() {
        val descriptor = FacetTypeDescriptor(typeKey = "structural", applicableTo = setOf(MetadataTargetType.TABLE, MetadataTargetType.ATTRIBUTE))
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.TABLE))
        assertTrue(descriptor.isApplicableTo(MetadataTargetType.ATTRIBUTE))
        assertFalse(descriptor.isApplicableTo(MetadataTargetType.CONCEPT))
        assertFalse(descriptor.isApplicableTo(MetadataTargetType.SCHEMA))
    }

    @Test
    fun shouldDetectContentSchema() {
        val withSchema = FacetTypeDescriptor(typeKey = "test", contentSchema = mapOf("type" to "object"))
        assertTrue(withSchema.hasContentSchema())
        val withoutSchema = FacetTypeDescriptor(typeKey = "test")
        assertFalse(withoutSchema.hasContentSchema())
        val emptySchema = FacetTypeDescriptor(typeKey = "test", contentSchema = emptyMap())
        assertFalse(emptySchema.hasContentSchema())
    }

    @Test
    fun shouldHaveCorrectDefaults() {
        val descriptor = FacetTypeDescriptor(typeKey = "test")
        assertFalse(descriptor.mandatory)
        assertTrue(descriptor.enabled)
    }
}
