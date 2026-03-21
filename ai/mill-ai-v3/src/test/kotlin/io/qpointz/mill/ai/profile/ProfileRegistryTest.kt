package io.qpointz.mill.ai.profile

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MapProfileRegistryTest {

    private val registry = MapProfileRegistry(
        HelloWorldAgentProfile.profile,
        SchemaExplorationAgentProfile.profile,
        SchemaAuthoringAgentProfile.profile,
    )

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
        assertTrue(registry.knownIds.containsAll(setOf("hello-world", "schema-exploration", "schema-authoring")))
    }

    @Test
    fun shouldAcceptVarargConstructor() {
        val r = MapProfileRegistry(HelloWorldAgentProfile.profile)
        assertNotNull(r.resolve("hello-world"))
    }
}

class DefaultProfileRegistryTest {

    @Test
    fun shouldResolveHelloWorld() {
        assertNotNull(DefaultProfileRegistry.resolve("hello-world"))
    }

    @Test
    fun shouldResolveSchemaExploration() {
        assertNotNull(DefaultProfileRegistry.resolve("schema-exploration"))
    }

    @Test
    fun shouldResolveSchemaAuthoring() {
        assertNotNull(DefaultProfileRegistry.resolve("schema-authoring"))
    }

    @Test
    fun shouldReturnNull_forUnknownProfileId() {
        assertNull(DefaultProfileRegistry.resolve("unknown"))
    }

    @Test
    fun shouldExposeAllKnownIds() {
        val ids = DefaultProfileRegistry.knownIds
        assertTrue(ids.containsAll(setOf("hello-world", "schema-exploration", "schema-authoring")))
    }
}
