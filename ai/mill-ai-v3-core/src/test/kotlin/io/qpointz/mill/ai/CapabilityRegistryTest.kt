package io.qpointz.mill.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CapabilityRegistryTest {
    @Test
    fun `should resolve capability specific dependencies from agent context`() {
        val registry = CapabilityRegistry.from(listOf(RequiredDependencyCapabilityProvider()))
        val profile = AgentProfile(id = "test-profile", capabilityIds = setOf("requires-dependency"))
        val context = AgentContext(
            contextType = "general",
            capabilityDependencies = CapabilityDependencyContainer.of(
                "requires-dependency" to CapabilityDependencies.of(FakeDependency("schema-access"))
            ),
        )

        val capability = registry.capabilitiesFor(profile, context).single()

        assertEquals("schema-access", capability.prompts.single().content)
    }

    @Test
    fun `should fail when required capability dependency is missing`() {
        val registry = CapabilityRegistry.from(listOf(RequiredDependencyCapabilityProvider()))
        val profile = AgentProfile(id = "test-profile", capabilityIds = setOf("requires-dependency"))

        val error = assertThrows(IllegalArgumentException::class.java) {
            registry.capabilitiesFor(profile, AgentContext(contextType = "general"))
        }

        assertEquals(
            "Missing dependencies for capability `requires-dependency`: [io.qpointz.mill.ai.CapabilityRegistryTest\$FakeDependency]",
            error.message,
        )
    }

    private data class FakeDependency(val value: String) : CapabilityDependency

    private class RequiredDependencyCapabilityProvider : CapabilityProvider {
        override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
            id = "requires-dependency",
            name = "Requires Dependency",
            description = "Test capability with a required dependency.",
            supportedContexts = setOf("general"),
            requiredDependencies = setOf(FakeDependency::class.java),
        )

        override fun create(
            context: AgentContext,
            dependencies: CapabilityDependencies,
        ): Capability = object : Capability {
            override val descriptor: CapabilityDescriptor = descriptor()
            override val prompts: List<PromptAsset> = listOf(
                PromptAsset(
                    id = "test.prompt",
                    description = "Echo resolved dependency.",
                    content = dependencies.require(FakeDependency::class.java).value,
                )
            )
            override val tools: List<ToolDefinition> = emptyList()
            override val protocols: List<ProtocolDefinition> = emptyList()
        }
    }
}
