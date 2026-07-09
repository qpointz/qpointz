package io.qpointz.mill.ai.profile

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ResourceProfileRegistryTest {

    @Test
    fun shouldLoadPlatformProfilesFromClasspath() {
        val registry = PlatformProfiles.registry()
        assertThat(registry.resolve("schema-authoring")).isNull()
        assertThat((registry as ResourceProfileRegistry).knownIds).containsExactlyInAnyOrder(
            "hello-world",
            "data-analysis",
            "schema-exploration",
            "metadata-authoring",
            "analysis-copilot",
        )
    }

    @Test
    fun shouldExposeDescription_whenPresent() {
        val profile = PlatformProfiles.require("metadata-authoring")
        assertThat(profile.description).contains("facet")
    }

    @Test
    fun shouldOverrideDuplicateId_withLaterResource() {
        val merged = ResourceProfileRegistry.load(
            ResourceProfileRegistryTest::class.java.classLoader,
            listOf(
                "classpath:profiles/platform-agent-profiles.yaml",
            ),
        )
        val override = ResourceProfileRegistry.parse(
            """
            kind: AgentProfile
            id: hello-world
            description: overridden
            capabilities:
              - conversation
              - demo
            """.trimIndent(),
        )
        val combined = ResourceProfileRegistry.parse(
            """
            kind: AgentProfile
            id: hello-world
            capabilities:
              - conversation
            ---
            kind: AgentProfile
            id: hello-world
            description: overridden
            capabilities:
              - conversation
              - demo
            """.trimIndent(),
        )
        val profile = combined.resolve("hello-world")!!
        assertThat(profile.description).isEqualTo("overridden")
        assertThat(profile.capabilityIds).contains("demo")
        assertThat(merged.resolve("hello-world")).isNotNull
        assertThat(override.resolve("hello-world")!!.capabilityIds).contains("demo")
    }

    @Test
    fun shouldLoadFromFileUri() {
        val yaml = """
            kind: AgentProfile
            id: file-profile
            capabilities:
              - conversation
        """.trimIndent()
        val file = kotlin.io.path.createTempFile(suffix = ".yaml").toFile()
        file.writeText(yaml)
        try {
            val registry = ResourceProfileRegistry.load(
                ResourceProfileRegistry.SeedInput { loc ->
                    java.io.File(loc.removePrefix("file:")).inputStream()
                },
                listOf("file:${file.absolutePath}"),
            )
            assertThat(registry.resolve("file-profile")).isNotNull
        } finally {
            file.delete()
        }
    }

    @Test
    fun shouldSkipUnknownKindDocuments() {
        val yaml = """
            kind: MetadataScope
            scopeUrn: urn:mill/metadata/scope:global
            ---
            kind: AgentProfile
            id: temp-profile
            capabilities:
              - conversation
        """.trimIndent()
        val registry = ResourceProfileRegistry.parse(yaml)
        assertThat(registry.knownIds).containsExactly("temp-profile")
    }
}
