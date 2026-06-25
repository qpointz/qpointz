package io.qpointz.mill.ai.profile

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MapProfileRegistryTest {

    private val hello = AgentProfile(id = "hello-world", capabilityIds = setOf("conversation", "demo"))

    private val registry = MapProfileRegistry(hello)

    @Test
    fun shouldResolveKnownProfile() {
        val profile = registry.resolve("hello-world")
        assertNotNull(profile)
        assertEquals("hello-world", profile?.id)
    }

    @Test
    fun shouldReturnNull_forUnknownProfileId() {
        assertNull(registry.resolve("no-such-profile"))
    }

    @Test
    fun shouldExposeKnownIds() {
        assertTrue(registry.knownIds.contains("hello-world"))
    }

    @Test
    fun shouldListRegisteredProfilesSortedById() {
        val ids = registry.registeredProfiles().map { it.id }
        assertEquals(listOf("hello-world"), ids)
    }

    @Test
    fun shouldAcceptVarargConstructor() {
        val r = MapProfileRegistry(hello)
        assertNotNull(r.resolve("hello-world"))
    }
}

class PlatformProfilesRegistryTest {

    private val registry = PlatformProfiles.registry()

    @Test
    fun shouldResolveHelloWorld() {
        assertNotNull(registry.resolve("hello-world"))
    }

    @Test
    fun shouldResolveDataAnalysis() {
        assertNotNull(registry.resolve("data-analysis"))
    }

    @Test
    fun shouldResolveSchemaExploration() {
        assertNotNull(registry.resolve("schema-exploration"))
    }

    @Test
    fun shouldResolveMetadataAuthoring() {
        assertNotNull(registry.resolve("metadata-authoring"))
    }

    @Test
    fun shouldNotShipSchemaAuthoringProfile() {
        assertNull(registry.resolve("schema-authoring"))
    }

    @Test
    fun shouldReturnNull_forUnknownProfileId() {
        assertNull(registry.resolve("unknown"))
    }

    @Test
    fun shouldExposeAllKnownIds() {
        val ids = (registry as ResourceProfileRegistry).knownIds
        assertTrue(ids.containsAll(setOf("hello-world", "data-analysis", "schema-exploration", "metadata-authoring")))
    }

    @Test
    fun shouldListRegisteredProfilesSortedById() {
        val ids = registry.registeredProfiles().map { it.id }
        assertEquals(listOf("data-analysis", "hello-world", "metadata-authoring", "schema-exploration"), ids)
    }
}
