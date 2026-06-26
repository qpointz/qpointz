package io.qpointz.mill.ai.mcp

import io.qpointz.mill.ai.capabilities.DemoCapabilityProvider
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.profile.PlatformProfiles
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CapabilityMcpCatalogTest {

    @Test
    fun shouldExposeNamespacedDemoTool() {
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.from(listOf(DemoCapabilityProvider())),
            exposureConfig = McpExposureConfig(capabilities = listOf("demo")),
        )
        assertThat(catalog.listToolNames()).contains("demo.say_hello")
    }

    @Test
    fun shouldOmitMcpDisabledManifestTools() {
        val provider = object : io.qpointz.mill.ai.core.capability.CapabilityProvider {
            override fun descriptor() = io.qpointz.mill.ai.core.capability.CapabilityDescriptor(
                id = "test-mcp-disabled",
                name = "Disabled",
                description = "Disabled MCP capability",
                supportedContexts = setOf("general"),
            )

            override fun create(
                context: io.qpointz.mill.ai.runtime.AgentContext,
                dependencies: io.qpointz.mill.ai.core.capability.CapabilityDependencies,
            ): io.qpointz.mill.ai.core.capability.Capability {
                error("not used in catalog tests")
            }
        }
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.from(listOf(provider)),
        )
        assertThat(catalog.listToolNames()).isEmpty()
    }

    @Test
    fun shouldApplyHelloWorldProfileFilter() {
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.load(),
            profile = PlatformProfiles.require("hello-world"),
        )
        assertThat(catalog.listToolNames()).contains("demo.say_hello")
        assertThat(catalog.listToolNames()).noneMatch { it.startsWith("schema.") }
    }

    @Test
    fun shouldApplyServerAllowlist() {
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.load(),
            exposureConfig = McpExposureConfig(capabilities = listOf("demo")),
        )
        assertThat(catalog.listToolNames()).allMatch { it.startsWith("demo.") }
    }

    @Test
    fun shouldExposeEveryMcpEnabledManifestToolWhenFiltersOpen() {
        val registry = CapabilityRegistry.load()
        val catalog = CapabilityMcpCatalog(registry = registry)
        assertThat(catalog.listToolNames())
            .containsExactlyElementsOf(expectedMcpToolNames(registry))
    }

    private fun expectedMcpToolNames(
        registry: CapabilityRegistry,
        exposureConfig: McpExposureConfig = McpExposureConfig(),
        profile: AgentProfile? = null,
    ): List<String> {
        val allowlist = exposureConfig.capabilities.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val profileIds = profile?.capabilityIds
        return registry.allDescriptors()
            .asSequence()
            .filter { descriptor -> profileIds == null || descriptor.id in profileIds }
            .filter { descriptor -> allowlist.isEmpty() || descriptor.id in allowlist }
            .flatMap { descriptor ->
                val manifest = CapabilityManifest.load(CapabilityManifest.manifestResourceFor(descriptor.id))
                if (!manifest.mcpSettings.enabled) {
                    emptySequence()
                } else {
                    manifest.declaredTools().asSequence().map { "${descriptor.id}.${it.name}" }
                }
            }
            .sorted()
            .toList()
    }
}
