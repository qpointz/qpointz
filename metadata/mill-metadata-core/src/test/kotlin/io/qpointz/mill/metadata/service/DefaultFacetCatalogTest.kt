package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetTargetCardinality
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultFacetCatalogTest {

    private lateinit var repository: InMemoryFacetTypeRepository
    private lateinit var catalog: DefaultFacetCatalog

    @BeforeEach
    fun setUp() {
        repository = InMemoryFacetTypeRepository()
        catalog = DefaultFacetCatalog(repository)
    }

    private fun mandatoryDescriptor(typeKey: String, vararg targets: String) =
        FacetTypeDescriptor(typeKey = typeKey, mandatory = true, enabled = true, displayName = typeKey,
            applicableTo = targets.toSet(), version = "1.0")

    private fun optionalDescriptor(typeKey: String, vararg targets: String) =
        FacetTypeDescriptor(typeKey = typeKey, mandatory = false, enabled = true, displayName = typeKey,
            applicableTo = if (targets.isNotEmpty()) targets.toSet() else null, version = "1.0")

    private fun optionalMultipleDescriptor(typeKey: String, vararg targets: String) =
        FacetTypeDescriptor(
            typeKey = typeKey,
            mandatory = false,
            targetCardinality = FacetTargetCardinality.MULTIPLE,
            enabled = true,
            displayName = typeKey,
            applicableTo = if (targets.isNotEmpty()) targets.toSet() else null,
            version = "1.0"
        )

    @Test fun shouldRegister_newFacetType() {
        catalog.register(optionalDescriptor("custom"))
        assertTrue(catalog.get("custom").isPresent)
    }

    @Test fun shouldReject_duplicateRegistration() {
        catalog.register(optionalDescriptor("custom"))
        assertThrows(IllegalArgumentException::class.java) { catalog.register(optionalDescriptor("custom")) }
    }

    @Test fun shouldUpdate_existingFacetType() {
        catalog.register(optionalDescriptor("custom"))
        catalog.update(optionalDescriptor("custom").copy(displayName = "Updated Custom"))
        assertEquals("Updated Custom", catalog.get("custom").orElseThrow().displayName)
    }

    @Test fun shouldReject_updateOfUnknownType() {
        assertThrows(IllegalArgumentException::class.java) { catalog.update(optionalDescriptor("nonexistent")) }
    }

    @Test fun shouldReject_disablingMandatoryType() {
        catalog.register(mandatoryDescriptor(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.ENTITY_TYPE_TABLE))
        assertThrows(IllegalArgumentException::class.java) {
            catalog.update(mandatoryDescriptor(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.ENTITY_TYPE_TABLE).copy(enabled = false))
        }
    }

    @Test fun shouldDelete_optionalType() {
        catalog.register(optionalDescriptor("custom"))
        catalog.delete("custom")
        assertTrue(catalog.get("custom").isEmpty)
    }

    @Test fun shouldReject_deletionOfMandatoryType() {
        catalog.register(mandatoryDescriptor(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.ENTITY_TYPE_TABLE))
        assertThrows(IllegalArgumentException::class.java) { catalog.delete(MetadataUrns.FACET_TYPE_STRUCTURAL) }
    }

    @Test fun shouldReturnAll_enabledTypes() {
        catalog.register(optionalDescriptor("enabled-one"))
        catalog.register(optionalDescriptor("disabled-one").copy(enabled = false))
        assertEquals(1, catalog.getEnabled().size)
    }

    @Test fun shouldReturnAll_mandatoryTypes() {
        catalog.register(mandatoryDescriptor("m1", MetadataUrns.ENTITY_TYPE_TABLE))
        catalog.register(optionalDescriptor("o1"))
        assertEquals(1, catalog.getMandatory().size)
    }

    @Test fun shouldFilter_byTargetType() {
        catalog.register(mandatoryDescriptor(MetadataUrns.FACET_TYPE_STRUCTURAL,
            MetadataUrns.ENTITY_TYPE_TABLE, MetadataUrns.ENTITY_TYPE_ATTRIBUTE))
        catalog.register(mandatoryDescriptor(MetadataUrns.FACET_TYPE_RELATION, MetadataUrns.ENTITY_TYPE_TABLE))
        catalog.register(optionalDescriptor(MetadataUrns.FACET_TYPE_CONCEPT, MetadataUrns.ENTITY_TYPE_CONCEPT))
        assertEquals(2, catalog.getForTargetType(MetadataUrns.ENTITY_TYPE_TABLE).size)
        assertEquals(1, catalog.getForTargetType(MetadataUrns.ENTITY_TYPE_CONCEPT).size)
    }

    @Test fun shouldAllowUnknownTypeKeys() {
        assertTrue(catalog.isAllowed("unknown-type"))
    }

    @Test fun shouldCheckApplicability() {
        catalog.register(mandatoryDescriptor(MetadataUrns.FACET_TYPE_STRUCTURAL,
            MetadataUrns.ENTITY_TYPE_TABLE, MetadataUrns.ENTITY_TYPE_ATTRIBUTE))
        assertTrue(catalog.isApplicableTo(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.ENTITY_TYPE_TABLE))
        assertFalse(catalog.isApplicableTo(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.ENTITY_TYPE_CONCEPT))
        assertTrue(catalog.isApplicableTo("unknown", MetadataUrns.ENTITY_TYPE_TABLE))
    }

    @Test fun shouldValidateEntityFacets_targetTypeMismatch() {
        catalog.register(mandatoryDescriptor(MetadataUrns.FACET_TYPE_STRUCTURAL,
            MetadataUrns.ENTITY_TYPE_TABLE, MetadataUrns.ENTITY_TYPE_ATTRIBUTE))
        val entity = MetadataEntity(id = "test", type = MetadataType.CONCEPT)
        entity.setFacet(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.SCOPE_GLOBAL,
            mapOf("physicalName" to "TEST"))
        val result = catalog.validateEntityFacets(entity)
        assertFalse(result.valid)
        assertTrue(result.errors[0].contains("not applicable"))
    }

    @Test fun shouldPass_validEntityFacets() {
        catalog.register(mandatoryDescriptor(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.ENTITY_TYPE_TABLE))
        val entity = MetadataEntity(id = "test", type = MetadataType.TABLE)
        entity.setFacet(MetadataUrns.FACET_TYPE_STRUCTURAL, MetadataUrns.SCOPE_GLOBAL,
            mapOf("physicalName" to "TEST"))
        assertTrue(catalog.validateEntityFacets(entity).valid)
    }

    @Test fun shouldSkipValidation_forUnknownFacetTypes() {
        val entity = MetadataEntity(id = "test", type = MetadataType.TABLE)
        entity.setFacet("custom-unknown", MetadataUrns.SCOPE_GLOBAL, mapOf("key" to "value"))
        assertTrue(catalog.validateEntityFacets(entity).valid)
    }

    @Test fun shouldReject_disabledFacetType() {
        catalog.register(optionalDescriptor("disabled-facet").copy(enabled = false))
        val entity = MetadataEntity(id = "test", type = MetadataType.TABLE)
        entity.setFacet("disabled-facet", MetadataUrns.SCOPE_GLOBAL, mapOf("key" to "value"))
        val result = catalog.validateEntityFacets(entity)
        assertFalse(result.valid)
        assertTrue(result.errors[0].contains("disabled"))
    }

    @Test fun shouldReject_multipleScopedValues_whenSingleCardinality() {
        catalog.register(optionalDescriptor("single-facet", MetadataUrns.ENTITY_TYPE_TABLE))
        val entity = MetadataEntity(id = "test", type = MetadataType.TABLE)
        entity.setFacet("single-facet", MetadataUrns.SCOPE_GLOBAL, mapOf("k" to "v1"))
        entity.setFacet("single-facet", MetadataUrns.scopeUser("alice"), mapOf("k" to "v2"))
        val result = catalog.validateEntityFacets(entity)
        assertFalse(result.valid)
        assertTrue(result.errors[0].contains("single value per entity"))
    }

    @Test fun shouldAllow_multipleScopedValues_whenMultipleCardinality() {
        catalog.register(optionalMultipleDescriptor("multi-facet", MetadataUrns.ENTITY_TYPE_TABLE))
        val entity = MetadataEntity(id = "test", type = MetadataType.TABLE)
        entity.setFacet("multi-facet", MetadataUrns.SCOPE_GLOBAL, mapOf("k" to "v1"))
        entity.setFacet("multi-facet", MetadataUrns.scopeUser("alice"), mapOf("k" to "v2"))
        val result = catalog.validateEntityFacets(entity)
        assertTrue(result.valid)
    }
}
