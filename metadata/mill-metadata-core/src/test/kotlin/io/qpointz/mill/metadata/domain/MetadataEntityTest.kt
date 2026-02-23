package io.qpointz.mill.metadata.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MetadataEntityTest {

    @Test
    fun shouldCreateEntity_withBasicFields() {
        val entity = MetadataEntity(id = "test.entity", type = MetadataType.TABLE, schemaName = "moneta", tableName = "customers")
        assertEquals("test.entity", entity.id)
        assertEquals(MetadataType.TABLE, entity.type)
    }

    @Test
    fun shouldSetAndGetFacet_withScope() {
        val entity = MetadataEntity(id = "test.entity")
        entity.setFacet("descriptive", "global", mapOf("displayName" to "Test Table", "description" to "Test description"))
        val facet = entity.getFacet("descriptive", "global", Map::class.java)
        assertTrue(facet.isPresent)
        assertEquals("Test Table", (facet.get() as Map<*, *>)["displayName"])
    }

    @Test
    fun shouldGetMergedFacet_withUserScope() {
        val entity = MetadataEntity(id = "test.entity")
        entity.setFacet("descriptive", "global", mapOf("description" to "Global description"))
        entity.setFacet("descriptive", "user:alice@company.com", mapOf("description" to "User-specific description"))
        val merged = entity.getMergedFacet("descriptive", "alice@company.com", emptyList(), emptyList(), Map::class.java)
        assertTrue(merged.isPresent)
        assertEquals("User-specific description", (merged.get() as Map<*, *>)["description"])
    }

    @Test
    fun shouldGetFacetScopes() {
        val entity = MetadataEntity(id = "test.entity")
        entity.setFacet("descriptive", "global", emptyMap<String, Any>())
        entity.setFacet("descriptive", "user:alice@company.com", emptyMap<String, Any>())
        entity.setFacet("descriptive", "team:engineering", emptyMap<String, Any>())
        val scopes = entity.getFacetScopes("descriptive")
        assertEquals(3, scopes.size)
        assertTrue(scopes.contains("global"))
        assertTrue(scopes.contains("user:alice@company.com"))
        assertTrue(scopes.contains("team:engineering"))
    }
}
