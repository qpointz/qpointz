package io.qpointz.mill.metadata.domain

import io.qpointz.mill.metadata.service.MetadataContext
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
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.SCOPE_GLOBAL,
            mapOf("displayName" to "Test Table", "description" to "Test description"))
        val facet = entity.getFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.SCOPE_GLOBAL, Map::class.java)
        assertTrue(facet.isPresent)
        assertEquals("Test Table", (facet.get() as Map<*, *>)["displayName"])
    }

    @Test
    fun shouldGetMergedFacet_withUserScope() {
        val entity = MetadataEntity(id = "test.entity")
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.SCOPE_GLOBAL,
            mapOf("description" to "Global description"))
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.scopeUser("alice@company.com"),
            mapOf("description" to "User-specific description"))
        val merged = entity.getMergedFacet(
            MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            MetadataContext(listOf(MetadataUrns.SCOPE_GLOBAL, MetadataUrns.scopeUser("alice@company.com"))),
            Map::class.java)
        assertTrue(merged.isPresent)
        assertEquals("User-specific description", (merged.get() as Map<*, *>)["description"])
    }

    @Test
    fun shouldGetFacetScopes() {
        val entity = MetadataEntity(id = "test.entity")
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.SCOPE_GLOBAL, emptyMap<String, Any>())
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.scopeUser("alice@company.com"), emptyMap<String, Any>())
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.scopeTeam("engineering"), emptyMap<String, Any>())
        val scopes = entity.getFacetScopes(MetadataUrns.FACET_TYPE_DESCRIPTIVE)
        assertEquals(3, scopes.size)
        assertTrue(scopes.contains(MetadataUrns.SCOPE_GLOBAL))
        assertTrue(scopes.contains(MetadataUrns.scopeUser("alice@company.com")))
        assertTrue(scopes.contains(MetadataUrns.scopeTeam("engineering")))
    }
}
