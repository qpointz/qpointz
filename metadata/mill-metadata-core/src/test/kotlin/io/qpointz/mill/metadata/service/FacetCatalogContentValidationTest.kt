package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataTargetType
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FacetCatalogContentValidationTest {

    private lateinit var repository: InMemoryFacetTypeRepository
    private lateinit var catalog: DefaultFacetCatalog

    @BeforeEach
    fun setUp() {
        repository = InMemoryFacetTypeRepository()
        catalog = DefaultFacetCatalog(repository, JsonSchemaFacetContentValidator())
        repository.save(FacetTypeDescriptor(
            typeKey = "audit", mandatory = false, enabled = true,
            displayName = "Audit", applicableTo = setOf(MetadataTargetType.TABLE), version = "1.0",
            contentSchema = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "auditor" to mapOf("type" to "string"),
                    "status" to mapOf("type" to "string", "enum" to listOf("passed", "failed", "pending"))
                ),
                "required" to listOf("status")
            )
        ))
    }

    @Test fun shouldPass_validContent() {
        assertTrue(catalog.validateFacetContent("audit", mapOf("auditor" to "alice", "status" to "passed")).valid)
    }

    @Test fun shouldFail_missingRequired() {
        assertFalse(catalog.validateFacetContent("audit", mapOf("auditor" to "alice")).valid)
    }

    @Test fun shouldFail_invalidEnum() {
        assertFalse(catalog.validateFacetContent("audit", mapOf("status" to "invalid-value")).valid)
    }

    @Test fun shouldPass_unknownTypeKey() {
        assertTrue(catalog.validateFacetContent("unknown", mapOf("anything" to "goes")).valid)
    }

    @Test fun shouldValidateEntityFacets_withContentSchema() {
        val entity = MetadataEntity(id = "test", type = MetadataType.TABLE)
        entity.setFacet("audit", "global", mapOf("status" to "invalid-value"))
        assertFalse(catalog.validateEntityFacets(entity).valid)
    }

    @Test fun shouldPass_entityFacets_withValidContent() {
        val entity = MetadataEntity(id = "test", type = MetadataType.TABLE)
        entity.setFacet("audit", "global", mapOf("status" to "passed", "auditor" to "bob"))
        assertTrue(catalog.validateEntityFacets(entity).valid)
    }
}
