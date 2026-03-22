package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataUrns
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MetadataContextTest {

    // --- MetadataContext construction ---

    @Test
    fun shouldThrow_whenScopesListIsEmpty() {
        assertThrows<IllegalArgumentException> {
            MetadataContext(emptyList())
        }
    }

    @Test
    fun shouldCreate_whenOneScopeGiven() {
        val ctx = MetadataContext.of(MetadataUrns.SCOPE_GLOBAL)
        assertEquals(1, ctx.scopes.size)
        assertEquals(MetadataUrns.SCOPE_GLOBAL, ctx.scopes[0])
    }

    @Test
    fun shouldReturnGlobalContext_whenGlobalCalled() {
        val ctx = MetadataContext.global()
        assertEquals(listOf(MetadataUrns.SCOPE_GLOBAL), ctx.scopes)
    }

    // --- parse ---

    @Test
    fun shouldReturnGlobal_whenContextParamIsNull() {
        val ctx = MetadataContext.parse(null)
        assertEquals(MetadataContext.global(), ctx)
    }

    @Test
    fun shouldReturnGlobal_whenContextParamIsBlank() {
        val ctx = MetadataContext.parse("   ")
        assertEquals(MetadataContext.global(), ctx)
    }

    @Test
    fun shouldParseSlugs_whenContextParamProvided() {
        val ctx = MetadataContext.parse("global,user:alice")
        assertEquals(2, ctx.scopes.size)
        assertEquals(MetadataUrns.SCOPE_GLOBAL, ctx.scopes[0])
        assertEquals(MetadataUrns.scopeUser("alice"), ctx.scopes[1])
    }

    @Test
    fun shouldAcceptArbitraryScopeType_whenChatOrCustomScopeInContext() {
        val ctx = MetadataContext.parse("global,user:123,chat:lalala")
        assertEquals(3, ctx.scopes.size)
        // chat: is not a known scope type — normaliseScopePath will expand it with SCOPE_PREFIX
        assertTrue(ctx.scopes[2].startsWith(MetadataUrns.SCOPE_PREFIX))
    }

    @Test
    fun shouldIgnoreBlankSegments_whenContextParamHasTrailingComma() {
        val ctx = MetadataContext.parse("global,")
        assertEquals(1, ctx.scopes.size)
        assertEquals(MetadataUrns.SCOPE_GLOBAL, ctx.scopes[0])
    }

    // --- getMergedFacet via MetadataEntity ---

    @Test
    fun shouldReturnGlobalValue_whenOnlyOneScopeInContext() {
        val entity = buildEntityWithFacets()
        val ctx = MetadataContext.global()
        val result = entity.getMergedFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, ctx, Map::class.java)
        assertTrue(result.isPresent)
        assertEquals("Global description", (result.get() as Map<*, *>)["description"])
    }

    @Test
    fun shouldReturnUserValue_whenUserScopeIsLast() {
        val entity = buildEntityWithFacets()
        val ctx = MetadataContext(listOf(MetadataUrns.SCOPE_GLOBAL, MetadataUrns.scopeUser("alice")))
        val result = entity.getMergedFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, ctx, Map::class.java)
        assertTrue(result.isPresent)
        assertEquals("Alice description", (result.get() as Map<*, *>)["description"])
    }

    @Test
    fun shouldReturnLastMatchingScope_whenMultipleScopesGiven() {
        val entity = buildEntityWithFacets()
        // global → team → user; user is last and wins
        val ctx = MetadataContext(listOf(
            MetadataUrns.SCOPE_GLOBAL,
            MetadataUrns.scopeTeam("eng"),
            MetadataUrns.scopeUser("alice")
        ))
        val result = entity.getMergedFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, ctx, Map::class.java)
        assertEquals("Alice description", (result.get() as Map<*, *>)["description"])
    }

    @Test
    fun shouldReturnEmpty_whenNoScopeHasFacet() {
        val entity = MetadataEntity(id = "entity")
        val ctx = MetadataContext.global()
        val result = entity.getMergedFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, ctx, Map::class.java)
        assertFalse(result.isPresent)
    }

    @Test
    fun shouldIgnoreUnknownScope_whenNoFacetsExistForIt() {
        val entity = buildEntityWithFacets()
        // chat scope has no facets — only global contributes
        val ctx = MetadataContext.parse("global,chat:xyz")
        val result = entity.getMergedFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, ctx, Map::class.java)
        assertTrue(result.isPresent)
        assertEquals("Global description", (result.get() as Map<*, *>)["description"])
    }

    // --- helpers ---

    private fun buildEntityWithFacets(): MetadataEntity {
        val entity = MetadataEntity(id = "entity")
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.SCOPE_GLOBAL,
            mapOf("description" to "Global description"))
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.scopeTeam("eng"),
            mapOf("description" to "Eng team description"))
        entity.setFacet(MetadataUrns.FACET_TYPE_DESCRIPTIVE, MetadataUrns.scopeUser("alice"),
            mapOf("description" to "Alice description"))
        return entity
    }
}
